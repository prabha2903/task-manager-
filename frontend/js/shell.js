/**
 * shell.js — Must be the FIRST script tag on every protected page.
 *
 * Resolves paths using document.currentScript (the only 100% reliable
 * method), with a fallback that scans all <script src> tags for "shell.js".
 * This works regardless of Live Server root, subfolder depth, or port.
 */
(function () {

  // ── 1. Find this script's absolute URL ─────────────────────────────────
  var myUrl = '';

  // Best: currentScript is set synchronously while the script is executing
  if (document.currentScript && document.currentScript.src) {
    myUrl = document.currentScript.src;
  }

  // Fallback: scan all <script> tags for one whose src ends with shell.js
  if (!myUrl) {
    var all = document.getElementsByTagName('script');
    for (var i = 0; i < all.length; i++) {
      if (all[i].src && all[i].src.indexOf('shell.js') !== -1) {
        myUrl = all[i].src;
        break;
      }
    }
  }

  // Last-resort fallback: derive from window.location (works when HTML and
  // js/ sit at the same level as they do in this project)
  if (!myUrl) {
    var loc = window.location.href.replace(/\/[^/]*$/, '/'); // strip filename
    myUrl = loc + 'js/shell.js';
  }

  // ── 2. Derive the project root (parent of js/) ──────────────────────────
  // myUrl is something like  http://127.0.0.1:5500/frontend/js/shell.js
  // We want                  http://127.0.0.1:5500/frontend/
  // ── 2. Derive the project root (parent of js/) ──────────────────────────
var jsDir = myUrl.replace(/\/[^/]+$/, '/'); // …/frontend/js/ OR …/js/
var rootDir = jsDir.replace(/js\/$/, '');
  

  // ── 3. Inject Bootstrap CSS (before our CSS so we can override) ─────────
  if (!document.querySelector('link[data-bs-css]')) {
    var bs      = document.createElement('link');
    bs.rel      = 'stylesheet';
    bs.href     = 'https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css';
    bs.setAttribute('data-bs-css', '1');
    document.head.appendChild(bs);
  }

  // ── 4. Inject our style.css with the resolved absolute path ─────────────
  if (!document.querySelector('link[data-taskflow-css]')) {
    var link    = document.createElement('link');
    link.rel    = 'stylesheet';
    link.href   = rootDir + 'css/styles.css';
    link.setAttribute('data-taskflow-css', '1');
    document.head.appendChild(link);
    console.log('[shell.js] Loading CSS from:', link.href);
  }

  // ── 5. Load SockJS + STOMP for WebSocket notifications ──────────────────
  function loadScript(src, id, onload) {
    if (document.getElementById(id)) { if (onload) onload(); return; }
    var s    = document.createElement('script');
    s.id     = id;
    s.src    = src;
    s.async  = false; // preserve execution order
    if (onload) s.onload = onload;
    document.head.appendChild(s);
  }

  // SockJS first, then STOMP (STOMP depends on SockJS being present)
  loadScript(
    'https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js',
    'sockjs-script',
    function () {
      loadScript(
        'https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js',
        'stomp-script',
        function () {
          console.log('[shell.js] SockJS + STOMP loaded.');
        }
      );
    }
  );

})();