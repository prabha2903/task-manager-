(function () {

  // ── 1. Find this script's absolute URL ─────────────────────────────────
  var myUrl = '';

  if (document.currentScript && document.currentScript.src) {
    myUrl = document.currentScript.src;
  }

  if (!myUrl) {
    var all = document.getElementsByTagName('script');
    for (var i = 0; i < all.length; i++) {
      if (all[i].src && all[i].src.indexOf('shell.js') !== -1) {
        myUrl = all[i].src;
        break;
      }
    }
  }

  if (!myUrl) {
    var loc = window.location.href.replace(/\/[^/]*$/, '/');
    myUrl = loc + 'js/shell.js';
  }

  // ── 2. Root directory ──────────────────────────────────────────────────
  var rootDir = myUrl.split('/js/')[0] + '/';

  // ── 3. Bootstrap CSS ───────────────────────────────────────────────────
  if (!document.querySelector('link[data-bs-css]')) {
    var bs = document.createElement('link');
    bs.rel = 'stylesheet';
    bs.href = 'https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css';
    bs.setAttribute('data-bs-css', '1');
    document.head.appendChild(bs);
  }

  // ── 4. App CSS ─────────────────────────────────────────────────────────
  if (!document.querySelector('link[data-taskflow-css]')) {
    var link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = rootDir + 'css/styles.css';
    link.setAttribute('data-taskflow-css', '1');
    document.head.appendChild(link);
    console.log('[shell.js] Loading CSS from:', link.href);
  }

  // ── 5. Script loader helper ────────────────────────────────────────────
  function loadScript(src, id, onload) {
    if (document.getElementById(id)) { if (onload) onload(); return; }

    var s = document.createElement('script');
    s.id = id;
    s.src = src;
    s.async = false;
    if (onload) s.onload = onload;

    document.head.appendChild(s);
  }

 // ── 6. Load Theme JS FIRST ─────────────────────────────────────────────

// Apply saved theme immediately (prevents flash)
(function () {
  var saved = localStorage.getItem('theme') || 'dark';
  document.documentElement.setAttribute('data-theme', saved);
})();

// Load theme.js once
loadScript(rootDir + 'js/theme.js', 'theme-script', function () {
  console.log('[shell.js] Theme loaded');

  // Safe init (no double init)
  if (!window.Theme.__initialized && window.Theme && typeof window.Theme.init === 'function') {
    window.Theme.__initialized = true;
    window.Theme.init();
  }
});

  // ── 7. Load SockJS + STOMP ─────────────────────────────────────────────
  loadScript(
    'https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js',
    'sockjs-script',
    function () {
      loadScript(
        'https://cdn.jsdelivr.net/npm/stompjs/lib/stomp.min.js',
        'stomp-script',
        function () {
          console.log('[shell.js] SockJS + STOMP loaded.');
        }
      );
    }
  );

})();
