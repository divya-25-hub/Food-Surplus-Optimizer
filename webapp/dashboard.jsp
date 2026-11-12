<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    jakarta.servlet.http.HttpSession sessionObj = request.getSession(false);
    if (sessionObj == null || sessionObj.getAttribute("username") == null) {
        response.sendRedirect("login.html");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Dashboard | FoodShare</title><link rel="stylesheet" href="app.css"></head>
<body class="app" data-page="dashboard" data-title="Dashboard" data-sub="Overview of donations and recipient requests">
<div id="content">
  <section class="hero">
    <div style="position:relative;z-index:1"><h2 id="hello">Welcome back</h2>
      <p>Connect surplus food with the people who need it. Review what is waiting, then match and confirm distributions.</p></div>
    <a class="btn btn-light btn-lg" href="process-donation.html">Process Donation</a>
  </section>

  <div class="grid cols-4" style="margin-bottom:22px">
    <div class="card stat"><div class="stat-icon green" id="i1"></div><div><b id="sDon">-</b><span>Available donations</span></div></div>
    <div class="card stat"><div class="stat-icon blue" id="i2"></div><div><b id="sStack">-</b><span>Stack positions in use (of 5)</span></div></div>
    <div class="card stat"><div class="stat-icon amber" id="i3"></div><div><b id="sReq">-</b><span>Pending requests</span></div></div>
  </div>

  <div class="card card-pad" style="margin-bottom:22px">
    <h2 style="margin:0 0 16px;font-size:16.5px">Quick actions</h2>
    <div class="actions-grid" id="actions"></div>
  </div>

  <div class="grid cols-2">
    <div class="card"><div class="card-head"><div><h2>Latest donations</h2></div><a href="view-donations.html">View all</a></div><div class="table-wrap" id="tDon"></div></div>
    <div class="card"><div class="card-head"><div><h2>Request queue</h2></div><a href="view-requests.html">View all</a></div><div class="table-wrap" id="tReq"></div></div>
  </div>
</div>
<script src="app.js"></script>
<script>
document.getElementById('hello').textContent = 'Welcome back, ' + (FS.user.name || FS.user.username);
document.getElementById('i1').innerHTML = FS.icons.box;
document.getElementById('i2').innerHTML = FS.icons.layers;
document.getElementById('i3').innerHTML = FS.icons.users;
var A = [['add-donation.html','plus','Add Donation','Register surplus food'],
         ['add-request.html','plus','Add Request','Record a recipient need'],
         ['process-donation.html','truck','Process Donation','Match with a recipient'],
         ['process-request.html','check','Process Request','Match with a donor'],
         ['view-donations.html','box','Available Donations','See what is waiting'],
         ['view-requests.html','list','Pending Requests','See the request queue']];
document.getElementById('actions').innerHTML = A.map(function (a) {
  return '<a class="action" href="' + a[0] + '"><div class="ico">' + FS.icons[a[1]] + '</div><b>' + a[2] + '</b><span>' + a[3] + '</span></a>';
}).join('');

Promise.all([FS.api('viewDonations'), FS.api('viewStack'), FS.api('viewRequests')]).then(function (r) {
  var don = r[0], stack = r[1], reqs = r[2];
  document.getElementById('sDon').textContent = don.length;
  document.getElementById('sStack').textContent = stack.length;
  document.getElementById('sReq').textContent = Array.isArray(reqs) ? reqs.length : 0;
  var d = don.slice().reverse().slice(0, 5);
  document.getElementById('tDon').innerHTML = d.length ?
    '<table class="data"><thead><tr><th>Food</th><th>Qty</th><th>Donor</th></tr></thead><tbody>' + d.map(function (x) {
      return '<tr><td><b>' + FS.esc(x.foodName) + '</b></td><td>' + FS.esc(x.quantity) + ' kg</td><td>' + FS.esc(x.donorName) + '</td></tr>'; }).join('') + '</tbody></table>'
    : FS.empty('No donations yet', 'Add the first donation.', 'add-donation.html', 'Add Donation');
  var q = Array.isArray(reqs) ? reqs.slice(0, 5) : [];
  document.getElementById('tReq').innerHTML = q.length ?
    '<table class="data"><thead><tr><th>Recipient</th><th>Food</th><th>Qty</th></tr></thead><tbody>' + q.map(function (x) {
      return '<tr><td><b>' + FS.esc(x.recipientName) + '</b></td><td>' + FS.esc(x.foodRequired) + '</td><td>' + FS.esc(x.quantityRequired) + ' kg</td></tr>'; }).join('') + '</tbody></table>'
    : FS.empty('No pending requests', 'Add the first request.', 'add-request.html', 'Add Request');
}).catch(function (e) {
  console.error(e);
  document.getElementById('tDon').innerHTML = FS.error('Could not load data from the server.');
  document.getElementById('tReq').innerHTML = FS.error('Could not load data from the server.');
});
</script></body></html>
