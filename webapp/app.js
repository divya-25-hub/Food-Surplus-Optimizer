/* FoodShare shared UI: layout shell, API helper, toast, confirm dialog */
(function () {
  var user = null;
  try { user = JSON.parse(localStorage.getItem('loggedInUser')); } catch (e) {}
  if (!user) { location.replace('login.html'); return; }

  var I = {
    home:'<svg viewBox="0 0 24 24"><path d="M3 11l9-8 9 8"/><path d="M5 10v10h14V10"/></svg>',
    plus:'<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><path d="M12 8v8M8 12h8"/></svg>',
    box:'<svg viewBox="0 0 24 24"><path d="M21 8l-9-5-9 5 9 5 9-5z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/></svg>',
    layers:'<svg viewBox="0 0 24 24"><path d="M12 3l9 5-9 5-9-5 9-5z"/><path d="M3 13l9 5 9-5"/></svg>',
    users:'<svg viewBox="0 0 24 24"><circle cx="9" cy="8" r="3.5"/><path d="M2.5 20c0-3.6 2.9-6 6.5-6s6.5 2.4 6.5 6"/><path d="M16 4.6a3.5 3.5 0 010 6.8M18.5 14.3c1.8.8 3 2.7 3 5.7"/></svg>',
    list:'<svg viewBox="0 0 24 24"><path d="M8 6h13M8 12h13M8 18h13"/><circle cx="3.5" cy="6" r="1"/><circle cx="3.5" cy="12" r="1"/><circle cx="3.5" cy="18" r="1"/></svg>',
    check:'<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><path d="M8 12.5l3 3 5-6"/></svg>',
    tick:'<svg viewBox="0 0 24 24"><path d="M5 12.5l4.5 4.5L19 7"/></svg>',
    truck:'<svg viewBox="0 0 24 24"><path d="M2 6h12v10H2zM14 10h4l4 3v3h-8"/><circle cx="7" cy="18" r="2"/><circle cx="18" cy="18" r="2"/></svg>',
    out:'<svg viewBox="0 0 24 24"><path d="M9 4H5v16h4M16 8l4 4-4 4M20 12H9"/></svg>',
    menu:'<svg viewBox="0 0 24 24"><path d="M4 7h16M4 12h16M4 17h16"/></svg>',
    refresh:'<svg viewBox="0 0 24 24"><path d="M20 11a8 8 0 00-14.5-4M4 4v4h4M4 13a8 8 0 0014.5 4M20 20v-4h-4"/></svg>',
    warn:'<svg viewBox="0 0 24 24"><path d="M12 3l10 18H2L12 3z"/><path d="M12 10v5M12 18v.5"/></svg>',
    inbox:'<svg viewBox="0 0 24 24"><path d="M3 13l3-8h12l3 8v6H3z"/><path d="M3 13h5l1 3h6l1-3h5"/></svg>'
  };

  var NAV = [
    ['Overview', [['dashboard.jsp','Dashboard','home','dashboard']]],
    ['Donations', [['add-donation.html','Add Donation','plus','add-donation'],
                   ['view-donations.html','Available Donations','box','view-donations'],
                   ['view-stack.html','Donation Stack','layers','view-stack'],
                   ['process-donation.html','Process Donation','truck','process-donation']]],
    ['Requests',  [['add-request.html','Add Request','plus','add-request'],
                   ['view-requests.html','Pending Requests','users','view-requests'],
                   ['process-request.html','Process Request','check','process-request']]]
  ];

  function esc(v){return String(v==null?'':v).replace(/[&<>"']/g,function(c){return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c];});}

  function build() {
    var body = document.body, page = body.getAttribute('data-page');
    var content = document.getElementById('content');
    var nav = '';
    NAV.forEach(function (g) {
      nav += '<div class="nav-label">' + g[0] + '</div>';
      g[1].forEach(function (n) {
        nav += '<a href="' + n[0] + '"' + (n[3] === page ? ' class="active"' : '') + '>' + I[n[2]] + n[1] + '</a>';
      });
    });
    var name = user.name || user.username || 'User';
    var shell = document.createElement('div');
    shell.className = 'shell';
    shell.innerHTML =
      '<aside class="sidebar" id="sidebar">' +
        '<div class="brand"><div class="brand-logo">FS</div><div><b>FoodShare</b><small>Surplus Food Optimizer</small></div></div>' +
        '<nav class="nav">' + nav + '</nav>' +
        '<div class="side-user"><div class="avatar">' + esc(name.charAt(0).toUpperCase()) + '</div>' +
        '<div><strong>' + esc(name) + '</strong><span>@' + esc(user.username || '') + '</span></div></div>' +
      '</aside>' +
      '<div class="main">' +
        '<header class="topbar"><div style="display:flex;align-items:center;gap:12px">' +
          '<button class="menu-btn" id="menuBtn" aria-label="Menu">' + I.menu + '</button>' +
          '<div><h1>' + esc(body.getAttribute('data-title')) + '</h1><p>' + esc(body.getAttribute('data-sub') || '') + '</p></div></div>' +
          '<div class="top-actions"><button class="btn btn-outline" id="logoutBtn">' + I.out + 'Logout</button></div>' +
        '</header></div>';
    content.parentNode.insertBefore(shell, content);
    shell.querySelector('.main').appendChild(content);
    document.getElementById('menuBtn').onclick = function () { document.getElementById('sidebar').classList.toggle('open'); };
    document.getElementById('logoutBtn').onclick = FS.logout;

    var t = document.createElement('div'); t.id = 'toasts'; document.body.appendChild(t);
    var m = document.createElement('div'); m.className = 'modal-back'; m.id = 'modalBack'; document.body.appendChild(m);
  }

  var FS = window.FS = {
    icons: I, esc: esc, user: user,
    api: function (action, params) {
      var body = new URLSearchParams(Object.assign({ action: action }, params || {}));
      return fetch('foodshare', { method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: body.toString() })
        .then(function (r) { return r.json(); });
    },
    toast: function (msg, type) {
      var el = document.createElement('div');
      el.className = 'toast ' + (type || '');
      el.textContent = msg;
      document.getElementById('toasts').appendChild(el);
      setTimeout(function () { el.remove(); }, 4200);
    },
    /* Yes / No dialog. Resolves true for Yes, false for No. */
    confirm: function (title, html, yes, no) {
      return new Promise(function (resolve) {
        var back = document.getElementById('modalBack');
        back.innerHTML = '<div class="modal" role="dialog" aria-modal="true"><div class="m-ico">' + I.warn + '</div>' +
          '<h3>' + esc(title) + '</h3><p>' + html + '</p>' +
          '<div class="m-actions"><button class="btn btn-outline" id="mNo">' + esc(no || 'No') + '</button>' +
          '<button class="btn btn-primary" id="mYes">' + esc(yes || 'Yes') + '</button></div></div>';
        back.classList.add('show');
        function done(v) { back.classList.remove('show'); back.innerHTML = ''; document.removeEventListener('keydown', key); resolve(v); }
        function key(e) { if (e.key === 'Escape') done(false); }
        document.addEventListener('keydown', key);
        document.getElementById('mYes').onclick = function () { done(true); };
        document.getElementById('mNo').onclick = function () { done(false); };
        document.getElementById('mNo').focus();
      });
    },
    logout: function () {
      localStorage.removeItem('loggedInUser');
      fetch('auth?action=logout', { method: 'POST' }).finally(function () { location.replace('login.html'); });
    },
    loading: function () { return '<div class="loading-block"><div class="skeleton" style="width:40%"></div><div class="skeleton"></div><div class="skeleton" style="width:80%"></div></div>'; },
    empty: function (title, text, linkHref, linkText) {
      return '<div class="empty"><div class="ico">' + I.inbox + '</div><h3>' + esc(title) + '</h3><p>' + esc(text) + '</p>' +
        (linkHref ? '<a class="btn btn-primary" href="' + linkHref + '">' + esc(linkText) + '</a>' : '') + '</div>';
    },
    error: function (text) { return '<div class="empty"><div class="ico">' + I.warn + '</div><h3>Something went wrong</h3><p>' + esc(text) + '</p></div>'; }
  };

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', build); else build();
})();
