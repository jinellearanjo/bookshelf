// Injected into every chapter after load. Talks back to Kotlin via the
// AndroidBridge @JavascriptInterface registered in ReaderWebView.
(function () {
  'use strict';

  // ---- Pagination state ----
  // Scroll mode (default) is a plain vertical-scrolling page -- no setup needed.
  // Paginated mode reflows body into CSS columns sized to one screen each, and
  // "scrolling" becomes horizontal jumps of exactly one column+gap at a time.
  var isPaginated = false;
  var pageStepPx = 0;

  window.__setupPagination = function (marginPx) {
    isPaginated = true;
    var margin = marginPx || 16;
    var colWidth = window.innerWidth - margin * 2;
    pageStepPx = colWidth + margin * 2; // one page = one column + the gap after it
    var body = document.body;
    body.style.margin = '0';
    body.style.height = '100vh';
    body.style.overflow = 'hidden';
    body.style.boxSizing = 'border-box';
    body.style.columnWidth = colWidth + 'px';
    body.style.columnGap = (margin * 2) + 'px';
    body.style.columnFill = 'auto';
    body.style.paddingLeft = margin + 'px';
    body.style.paddingRight = margin + 'px';
  };

  window.__disablePagination = function () {
    isPaginated = false;
    var body = document.body;
    body.style.columnWidth = '';
    body.style.columnGap = '';
    body.style.columnFill = '';
    body.style.height = '';
    body.style.overflow = '';
  };

  // Called by tap-zone handlers in ReaderWebView. No-ops in scroll mode --
  // scroll-mode navigation is plain vertical scrolling, handled natively by the WebView.
  window.__nextPage = function () {
    if (!isPaginated) return;
    var doc = document.documentElement;
    var maxScroll = document.body.scrollWidth - window.innerWidth;
    if (doc.scrollLeft >= maxScroll - 5) {
      // Already on the last page of this chapter -- hand off to Kotlin to advance chapters.
      if (window.AndroidBridge) window.AndroidBridge.onRequestNextChapter();
      return;
    }
    doc.scrollLeft = Math.min(doc.scrollLeft + pageStepPx, maxScroll);
    reportProgress();
  };

  window.__prevPage = function () {
    if (!isPaginated) return;
    var doc = document.documentElement;
    if (doc.scrollLeft <= 5) {
      if (window.AndroidBridge) window.AndroidBridge.onRequestPrevChapter();
      return;
    }
    doc.scrollLeft = Math.max(doc.scrollLeft - pageStepPx, 0);
    reportProgress();
  };

  function reportProgress() {
    var doc = document.documentElement;
    var percent;
    if (isPaginated) {
      var maxScroll = document.body.scrollWidth - window.innerWidth;
      percent = maxScroll > 0 ? (doc.scrollLeft / maxScroll) : 0;
    } else {
      var scrollable = doc.scrollHeight - doc.clientHeight;
      percent = scrollable > 0 ? (doc.scrollTop / scrollable) : 0;
    }
    if (window.AndroidBridge) window.AndroidBridge.onScrollProgress(percent);
  }

  // ---- Plain-text offset mapping ----
  // Highlights are stored as character offsets into body.innerText (not DOM position),
  // since DOM position shifts with any layout/reflow change but plain-text offsets
  // stay stable across font-size/theme/pagination changes as long as the text itself
  // doesn't change.

  function getPlainTextOffset(container, targetNode, targetOffset) {
    var walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT, null, false);
    var total = 0;
    var node;
    while ((node = walker.nextNode())) {
      if (node === targetNode) return total + targetOffset;
      total += node.textContent.length;
    }
    return total;
  }

  // ---- Text selection -> highlight creation ----

  document.addEventListener('selectionchange', function () {
    var sel = window.getSelection();
    if (!sel || sel.isCollapsed || sel.rangeCount === 0) return;
    var range = sel.getRangeAt(0);
    var text = sel.toString();
    if (!text || text.trim().length === 0) return;

    var start = getPlainTextOffset(document.body, range.startContainer, range.startOffset);
    var end = getPlainTextOffset(document.body, range.endContainer, range.endOffset);

    if (window.AndroidBridge) {
      window.AndroidBridge.onTextSelected(start, end, text);
    }
  });

  // ---- Scroll position -> progress tracking ----
  // Fires for both vertical (scroll mode) and horizontal (paginated mode) movement --
  // the 'scroll' event covers scrollLeft changes too, so one listener handles both.

  var scrollReportTimer = null;
  window.addEventListener('scroll', function () {
    if (scrollReportTimer) clearTimeout(scrollReportTimer);
    scrollReportTimer = setTimeout(reportProgress, 200);
  });

  // ---- Restore scroll/page position after chapter load ----

  window.__restoreScroll = function (percent) {
    var doc = document.documentElement;
    if (isPaginated) {
      var maxScroll = document.body.scrollWidth - window.innerWidth;
      var target = maxScroll * percent;
      // Snap to the nearest page boundary so we never land mid-column.
      var pageIndex = pageStepPx > 0 ? Math.round(target / pageStepPx) : 0;
      doc.scrollLeft = Math.min(pageIndex * pageStepPx, Math.max(maxScroll, 0));
    } else {
      var scrollable = doc.scrollHeight - doc.clientHeight;
      doc.scrollTop = scrollable * percent;
    }
  };

  // ---- Scroll to a TOC fragment (href="chapter.xhtml#section-2") ----
  // Handles both id="section-2" (EPUB3-typical) and the older name="section-2"
  // anchor style some EPUB2 books still use. Works in both page modes: in scroll
  // mode scrollIntoView moves vertically as normal; in paginated mode the browser
  // resolves scrollIntoView's horizontal case for a column-laid-out element too,
  // and the explicit snap below cleans up any sub-pixel rounding so we still land
  // exactly on a page boundary rather than half a page off.

  window.__scrollToFragment = function (fragmentId) {
    if (!fragmentId) return;
    var el = document.getElementById(fragmentId);
    if (!el) {
      var named = document.getElementsByName(fragmentId);
      if (named && named.length > 0) el = named[0];
    }
    if (!el) return; // id not found in this chapter -- caller falls back to top-of-chapter

    el.scrollIntoView({ block: 'start', inline: 'start' });

    if (isPaginated) {
      var doc = document.documentElement;
      var maxScroll = document.body.scrollWidth - window.innerWidth;
      var pageIndex = pageStepPx > 0 ? Math.round(doc.scrollLeft / pageStepPx) : 0;
      doc.scrollLeft = Math.min(pageIndex * pageStepPx, Math.max(maxScroll, 0));
    }

    // Let Kotlin know where we actually ended up, so it's saved as the chapter's
    // progress -- otherwise the next unrelated reload (e.g. a font-size change)
    // would restore to wherever progress last pointed, undoing this jump.
    reportProgress();
  };

  // ---- Highlight rendering ----
  // Wraps [startOffset, endOffset) of body.innerText in a <mark> with the given color.
  // Applied for every saved highlight on chapter load, and immediately after a new one is made.
  // Works identically in both page modes since it operates on plain-text offsets, not
  // scroll position.

  window.__applyHighlight = function (startOffset, endOffset, colorHex, highlightId) {
    var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
    var total = 0;
    var node;
    var startNode = null, startNodeOffset = 0;
    var endNode = null, endNodeOffset = 0;

    while ((node = walker.nextNode())) {
      var len = node.textContent.length;
      if (startNode === null && total + len >= startOffset) {
        startNode = node;
        startNodeOffset = startOffset - total;
      }
      if (total + len >= endOffset) {
        endNode = node;
        endNodeOffset = endOffset - total;
        break;
      }
      total += len;
    }

    if (!startNode || !endNode) return; // offsets no longer valid -- caller falls back to text search

    try {
      var range = document.createRange();
      range.setStart(startNode, startNodeOffset);
      range.setEnd(endNode, endNodeOffset);

      var mark = document.createElement('mark');
      mark.style.backgroundColor = colorHex;
      mark.dataset.highlightId = highlightId;
      try {
        range.surroundContents(mark);
      } catch (e) {
        var contents = range.extractContents();
        mark.appendChild(contents);
        range.insertNode(mark);
      }
    } catch (e) {
      console.warn('Failed to apply highlight', highlightId, e);
    }
  };

  // Tap on a <mark> reopens it for edit/delete via the bridge.
  document.addEventListener('click', function (e) {
    var mark = e.target.closest && e.target.closest('mark');
    if (mark && mark.dataset.highlightId && window.AndroidBridge) {
      window.AndroidBridge.onHighlightTapped(mark.dataset.highlightId);
    }
  });

  // Removes a single highlight's <mark> wrapper (unwrapping its text back into the
  // surrounding content) without touching anything else -- used when a highlight is
  // deleted but the chapter itself doesn't need a full reload.
  window.__removeHighlight = function (highlightId) {
    var mark = document.querySelector('mark[data-highlight-id="' + highlightId + '"]');
    if (!mark) return;
    var parent = mark.parentNode;
    if (!parent) return;
    while (mark.firstChild) parent.insertBefore(mark.firstChild, mark);
    parent.removeChild(mark);
    parent.normalize();
  };
})();
