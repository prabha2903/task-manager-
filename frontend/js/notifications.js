// ============================================================
// notifications.js — WebSocket via STOMP + SockJS
// SockJS and STOMP are loaded asynchronously by shell.js.
// This file polls until they're ready before connecting.
// ============================================================

var Notifications = {
  stompClient:   null,
  notifications: [],
  unreadCount:   0,
  bellBtn:       null,
  badge:         null,
  dropdown:      null,
  listEl:        null,
  _connectTries: 0,

  // ── Public: called by initPage() ─────────────────────────
  init: function () {
    this.bellBtn  = document.getElementById('notif-btn');
    this.badge    = document.getElementById('notif-badge');
    this.dropdown = document.getElementById('notif-dropdown');
    this.listEl   = document.getElementById('notif-list');

    if (!this.bellBtn) return;

    var self = this;

    this.bellBtn.addEventListener('click', function (e) {
      e.stopPropagation();
      self.toggleDropdown();
    });

    document.addEventListener('click', function (e) {
      if (!self.dropdown) return;
      if (!self.bellBtn.contains(e.target) && !self.dropdown.contains(e.target)) {
        self.dropdown.classList.remove('open');
      }
    });

    // Restore persisted notifications
    try {
      var saved = localStorage.getItem('tf_notifications');
      if (saved) {
        this.notifications = JSON.parse(saved);
        this.unreadCount   = this.notifications.filter(function (n) { return !n.read; }).length;
        this.renderList();
        this.updateBadge();
      }
    } catch (e) { /* ignore */ }

    // Wait for SockJS + STOMP then connect
    this._connectWhenReady();
  },

  // ── Poll until SockJS + STOMP are available (max 10 s) ───
  _connectWhenReady: function () {
    var self = this;
    if (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
      this.connect();
      return;
    }
    this._connectTries++;
    if (this._connectTries > 40) {
      console.warn('[Notifications] SockJS/STOMP did not load in 10 s — WebSocket disabled.');
      return;
    }
    setTimeout(function () { self._connectWhenReady(); }, 250);
  },

  connect: function () {
    var token = Api.getToken();
    if (!token) return;

    var self = this;
    try {
      var socket       = new SockJS('http://localhost:8080/ws');
      this.stompClient = Stomp.over(socket);
      this.stompClient.debug = null;

      this.stompClient.connect(
        { Authorization: 'Bearer ' + token },
        function () {
          console.log('[Notifications] WebSocket connected');

          self.stompClient.subscribe('/topic/notifications', function (msg) {
            var data;
            try { data = JSON.parse(msg.body); }
            catch (e) { data = { message: msg.body, type: 'info' }; }
            self.onMessage(data);
          });

          var user = Api.getUser();
          if (user && user.id) {
            self.stompClient.subscribe('/user/' + user.id + '/notifications', function (msg) {
              var data;
              try { data = JSON.parse(msg.body); }
              catch (e) { data = { message: msg.body, type: 'info' }; }
              self.onMessage(data);
            });
          }
        },
        function (err) {
          console.warn('[Notifications] WebSocket error, retrying in 15 s…', err);
          setTimeout(function () { self.connect(); }, 15000);
        }
      );
    } catch (e) {
      console.warn('[Notifications] Failed to create WebSocket:', e);
    }
  },

  disconnect: function () {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect();
    }
  },

  onMessage: function (data) {
    var notif = {
      id:      Date.now(),
      title:   data.title   || 'Notification',
      message: data.message || JSON.stringify(data),
      type:    data.type    || 'info',
      time:    new Date().toISOString(),
      read:    false,
    };
    this.notifications.unshift(notif);
    if (this.notifications.length > 50) this.notifications.pop();
    this.unreadCount++;
    this.saveToStorage();
    this.renderList();
    this.updateBadge();
    Toast.show(notif.message, notif.type, notif.title);
  },

  markAllRead: function () {
    this.notifications.forEach(function (n) { n.read = true; });
    this.unreadCount = 0;
    this.saveToStorage();
    this.updateBadge();
    this.renderList();
  },

  toggleDropdown: function () {
    if (!this.dropdown) return;
    var open = this.dropdown.classList.contains('open');
    if (!open) { this.dropdown.classList.add('open'); this.markAllRead(); }
    else        { this.dropdown.classList.remove('open'); }
  },

  updateBadge: function () {
    if (!this.badge) return;
    if (this.unreadCount > 0) {
      this.badge.textContent = this.unreadCount > 99 ? '99+' : String(this.unreadCount);
      this.badge.classList.add('show');
    } else {
      this.badge.classList.remove('show');
    }
  },

  renderList: function () {
    if (!this.listEl) return;
    if (!this.notifications.length) {
      this.listEl.innerHTML = '<div class="notif-empty">🔕 No notifications yet</div>';
      return;
    }
    var self = this;
    this.listEl.innerHTML = this.notifications.slice(0, 10).map(function (n) {
      return '<div class="notif-item">' +
        '<div class="notif-item-title">' + self._esc(n.message) + '</div>' +
        '<div class="notif-item-time">'  + self._timeAgo(n.time) + '</div>' +
        '</div>';
    }).join('');
  },

  saveToStorage: function () {
    try { localStorage.setItem('tf_notifications', JSON.stringify(this.notifications.slice(0, 50))); }
    catch (e) { /* quota */ }
  },

  _timeAgo: function (iso) {
    var diff = Date.now() - new Date(iso).getTime();
    if (diff < 60000)    return 'Just now';
    if (diff < 3600000)  return Math.floor(diff / 60000)   + 'm ago';
    if (diff < 86400000) return Math.floor(diff / 3600000) + 'h ago';
    return Math.floor(diff / 86400000) + 'd ago';
  },

  _esc: function (str) {
    return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
  },
};

window.Notifications = Notifications;