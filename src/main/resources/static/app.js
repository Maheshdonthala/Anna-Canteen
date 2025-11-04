async function getCsrf() {
  const tokenMeta = document.querySelector('meta[name="_csrf"]');
  const headerMeta = document.querySelector('meta[name="_csrf_header"]');
  return {
    header: headerMeta ? headerMeta.getAttribute('content') : null,
    token: tokenMeta ? tokenMeta.getAttribute('content') : null
  };
}

// Attendance page
if (document.getElementById('attendanceForm')) {
  const path = window.location.pathname.split('/');
  const canteenId = path[2];
  async function loadWorkers() {
    try {
      const res = await fetch(`/api/canteen/${canteenId}/workers`);
      if (!res.ok) return;
      const workers = await res.json();
      const sel = document.getElementById('workerSelect');
      sel.innerHTML = '<option value="">-- Select worker --</option>';
      workers.forEach(w => {
        const o = document.createElement('option'); o.value = w.id; o.textContent = w.name + ' (' + (w.role || '') + ')'; sel.appendChild(o);
      });
    } catch (err) { console.warn('failed loading workers', err); }
  }

  // Render worker management list/form if present
  async function renderWorkerManagement() {
    const container = document.getElementById('workerList');
    const form = document.getElementById('workerForm');
    if (!container || !form) return;
    try {
      const res = await fetch(`/api/canteen/${canteenId}/workers`);
      if (!res.ok) { container.textContent = '(failed to load)'; return; }
      const list = await res.json();
      container.innerHTML = '';
      list.forEach(w => {
        const row = document.createElement('div'); row.className = 'list-row';
        const left = document.createElement('div'); left.textContent = (w.name || '') + ' — ' + (w.role || '');
        const right = document.createElement('div');
        const edit = document.createElement('button'); edit.className = 'btn'; edit.textContent = 'Edit';
        edit.addEventListener('click', () => {
          document.getElementById('workerId').value = w.id;
          document.getElementById('workerName').value = w.name || '';
          document.getElementById('workerRole').value = w.role || '';
        });
        const del = document.createElement('button'); del.className = 'btn'; del.textContent = 'Delete';
        del.addEventListener('click', async () => {
          if (!confirm('Delete worker "' + w.name + '"?')) return;
          const csrf = await getCsrf();
          const headers = {};
          if (csrf.token && csrf.header) headers[csrf.header] = csrf.token;
          const r = await fetch(`/api/canteen/${canteenId}/workers/${w.id}`, { method: 'DELETE', headers });
          if (r.ok) { renderWorkerManagement(); loadWorkers(); } else alert('Delete failed: '+r.status);
        });
        right.appendChild(edit); right.appendChild(document.createTextNode(' ')); right.appendChild(del);
        row.appendChild(left); row.appendChild(right); container.appendChild(row);
      });
    } catch (err) { console.warn('failed loading worker management', err); }
  }

  // Render simple read-only list of all workers (for the Workers button)
  async function renderAllWorkersList() {
    const container = document.getElementById('allWorkersList');
    if (!container) return;
    try {
      container.textContent = '(loading...)';
      const res = await fetch(`/api/canteen/${canteenId}/workers`);
      if (!res.ok) { container.textContent = '(failed to load)'; return; }
      const list = await res.json();
      if (!Array.isArray(list) || list.length === 0) {
        container.innerHTML = '(no workers) ';
        const btn = document.createElement('button'); btn.className = 'btn'; btn.textContent = 'Import legacy workers here';
        btn.addEventListener('click', async () => {
          try {
            const csrf = await getCsrf();
            const headers = {};
            if (csrf.token && csrf.header) headers[csrf.header] = csrf.token;
            const r = await fetch(`/api/canteen/${canteenId}/workers/assign-legacy`, { method: 'POST', headers });
            if (r.ok) {
              const count = await r.json().catch(() => 0);
              alert(`Assigned ${count} worker(s) to this canteen.`);
              renderAllWorkersList();
              // also refresh the management list
              if (typeof renderWorkerManagement === 'function') renderWorkerManagement();
            } else {
              alert('Assignment failed: ' + r.status);
            }
          } catch (e) { console.error(e); alert('Assignment failed'); }
        });
        container.appendChild(btn);
        return;
      }
      container.innerHTML = '';
      list.forEach(w => {
        const row = document.createElement('div'); row.className = 'list-row';
        const left = document.createElement('div'); left.textContent = (w.name || '') + (w.role ? ' — ' + w.role : '');
        row.appendChild(left);
        container.appendChild(row);
      });
    } catch (err) {
      console.warn('failed loading all workers', err);
      container.textContent = '(failed to load)';
    }
  }

  // worker form handlers
  if (document.getElementById('workerForm')) {
    document.getElementById('workerForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const id = document.getElementById('workerId').value;
      const name = document.getElementById('workerName').value;
      const role = document.getElementById('workerRole').value;
      if (!name || !role) { alert('Name and role required'); return; }
      const csrf = await getCsrf();
      const headers = {'Content-Type':'application/json'};
      if (csrf.token && csrf.header) headers[csrf.header] = csrf.token;
      const payload = { name, role };
      let res;
      if (id) {
        res = await fetch(`/api/canteen/${canteenId}/workers/${id}`, { method: 'PUT', headers, body: JSON.stringify(payload) });
      } else {
        res = await fetch(`/api/canteen/${canteenId}/workers`, { method: 'POST', headers, body: JSON.stringify(payload) });
      }
      if (res.ok) {
        document.getElementById('workerForm').reset();
        document.getElementById('workerId').value = '';
        renderWorkerManagement();
        loadWorkers();
      } else {
        alert('Save failed: ' + res.status);
      }
    });

    document.getElementById('cancelWorker').addEventListener('click', (e) => {
      e.preventDefault();
      document.getElementById('workerForm').reset();
      document.getElementById('workerId').value = '';
    });

    // initial render
    renderWorkerManagement();
  }

  async function loadAttendanceList() {
    const res = await fetch(`/api/canteen/${canteenId}/attendance`);
    if (!res.ok) { document.getElementById('attendanceList').textContent = '(failed to load)'; return; }
    const list = await res.json();
    const container = document.getElementById('attendanceList');
    container.innerHTML = '';
    list.forEach(a => {
      const row = document.createElement('div'); row.className = 'list-row';
      const left = document.createElement('div'); left.textContent = (a.workerName || a.workerId) + ' — ' + new Date(a.date).toLocaleString();
      const right = document.createElement('div'); right.innerHTML = `<span class="badge ${a.status.toLowerCase()}">${a.status}</span>`;
      row.appendChild(left); row.appendChild(right); container.appendChild(row);
    });
  }

  loadWorkers();
  loadAttendanceList();

  // Workers button toggle behavior
  const workersBtn = document.getElementById('showWorkersBtn');
  const allWorkersSection = document.getElementById('allWorkersSection');
  const closeAllWorkers = document.getElementById('closeAllWorkers');
  if (workersBtn && allWorkersSection) {
    workersBtn.addEventListener('click', () => {
      allWorkersSection.style.display = '';
      renderAllWorkersList();
      workersBtn.blur();
    });
  }
  if (closeAllWorkers && allWorkersSection) {
    closeAllWorkers.addEventListener('click', () => {
      allWorkersSection.style.display = 'none';
    });
  }

  document.getElementById('attendanceForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const workerId = document.getElementById('workerSelect').value;
    const status = document.getElementById('status').value;
    const csrf = await getCsrf();
    const headers = {'Content-Type':'application/json'};
    if (csrf.token && csrf.header) headers[csrf.header] = csrf.token;
    const res = await fetch(`/api/canteen/${canteenId}/attendance`, { method:'POST', headers, body: JSON.stringify({ workerId, status }) });
    if (res.ok) {
      alert('Attendance recorded');
      loadAttendanceList();
      // Also refresh the existing workers list so it's always up-to-date
      if (typeof renderWorkerManagement === 'function') {
        renderWorkerManagement();
      }
    } else {
      alert('Failed: '+res.status);
    }
  });
}

// Salary page
if (document.getElementById('markSalaryForm')) {
  const path = window.location.pathname.split('/');
  const canteenId = path[2];
  async function loadSalaryWorkers() {
    try {
      const res = await fetch(`/api/canteen/${canteenId}/workers`);
      if (!res.ok) return;
      const workers = await res.json();
      const sel = document.getElementById('salaryWorkerSelect');
      sel.innerHTML = '<option value="">-- Select worker --</option>';
      workers.forEach(w => { const o = document.createElement('option'); o.value = w.id; o.textContent = w.name + ' (' + (w.role || '') + ')'; sel.appendChild(o); });
    } catch (err) { console.warn('failed loading workers', err); }
  }

  async function renderSalaryList(status) {
    const res = await fetch(`/api/canteen/${canteenId}/salaries/status/${status}`);
    if (!res.ok) { document.getElementById('salaryList').textContent = '(failed to load)'; return; }
    const list = await res.json();
    const container = document.getElementById('salaryList'); container.innerHTML = '';
    list.forEach(s => {
      const row = document.createElement('div'); row.className = 'list-row';
      const left = document.createElement('div'); left.textContent = (s.workerName || s.workerId) + ' — ' + (s.month || '') + '/' + (s.year || '');
      const right = document.createElement('div'); right.innerHTML = `<span class="badge ${s.status.toLowerCase()}">${s.status}</span>`;
      row.appendChild(left); row.appendChild(right); container.appendChild(row);
    });
  }

  loadSalaryWorkers();
  renderSalaryList('PENDING');

  document.getElementById('markSalaryForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const workerId = document.getElementById('salaryWorkerSelect').value;
    const status = document.getElementById('salaryStatus').value;
    if (!workerId) { alert('Select a worker'); return; }
    const csrf = await getCsrf();
    const headers = {'Content-Type':'application/json'};
    if (csrf.token && csrf.header) headers[csrf.header] = csrf.token;
    const res = await fetch(`/api/canteen/${canteenId}/salaries/mark`, { method:'POST', headers, body: JSON.stringify({ workerId, status }) });
    if (res.ok) {
      alert('Salary status updated successfully!');
      // Refresh the current list view
      const currentList = document.querySelector('.btn-row .btn.active') || document.getElementById('viewPending');
      currentList.click();
    } else {
      alert('Failed to update salary: ' + res.status);
    }
  });

  document.getElementById('viewPaid').addEventListener('click', async (e) => {
    document.getElementById('viewPaid').classList.add('active');
    document.getElementById('viewPending').classList.remove('active');
    renderSalaryList('PAID');
    e.target.blur();
  });
  document.getElementById('viewPending').addEventListener('click', async (e) => {
    document.getElementById('viewPending').classList.add('active');
    document.getElementById('viewPaid').classList.remove('active');
    renderSalaryList('PENDING');
    e.target.blur();
  });
}

// Canteens page
if (document.getElementById('canteenList')) {
  async function loadCanteens() {
    const res = await fetch('/api/canteens');
    const list = await res.json();
    const sidebar = document.getElementById('canteenList');
    sidebar.innerHTML = '';
    // also populate the cards area
    const cards = document.getElementById('canteensCards');
    if (cards) cards.innerHTML = '';
    list.forEach((c, idx) => {
  // sidebar numbered button (centered text, no trailing 'Open')
  const btn = document.createElement('a');
  btn.className = 'canteen-btn';
  btn.href = `/canteen/${c.id}`;
  btn.innerHTML = `<span class="label"><span class="num">${idx+1}</span><span>${c.name}</span></span>`;
  sidebar.appendChild(btn);
        btn.innerHTML = `<span class="label"><span class="name">${c.name}</span></span>`;
      // cards grid
      if (cards) {
        const card = document.createElement('div'); card.className = 'card';
        const h = document.createElement('h3'); h.textContent = c.name;
        const p = document.createElement('p'); p.textContent = c.location;
  const a = document.createElement('button');
  a.className = 'btn';
  a.textContent = 'Open';
  a.addEventListener('click', () => { window.location.href = `/canteen/${c.id}`; });
        // edit button
        const editBtn = document.createElement('button'); editBtn.className = 'btn'; editBtn.textContent = 'Edit';
        editBtn.addEventListener('click', async () => {
          // simple prompt-based edit
          const newName = prompt('Canteen name:', c.name);
          if (newName === null) return; // cancelled
          const newLocation = prompt('Location:', c.location || '');
          if (newLocation === null) return;
          const currentPrice = (typeof c.defaultPlatePrice === 'number') ? c.defaultPlatePrice : 5.0;
          const newPriceStr = prompt('Default plate price:', String(currentPrice));
          if (newPriceStr === null) return;
          const newPrice = parseFloat(newPriceStr);
          if (Number.isNaN(newPrice) || newPrice < 0) { alert('Invalid price'); return; }
          try {
            const csrf = await getCsrf();
            const headers = { 'Content-Type': 'application/json' };
            if (csrf.token && csrf.header) headers[csrf.header] = csrf.token;
            const res = await fetch('/api/canteens/' + c.id, { method: 'PUT', headers, body: JSON.stringify({ name: newName, location: newLocation, defaultPlatePrice: newPrice }) });
            if (res.ok) { loadCanteens(); } else { alert('Update failed: ' + res.status); }
          } catch (err) { console.error('update failed', err); alert('Update failed'); }
        });

  // delete button
  const del = document.createElement('button'); del.className = 'btn action'; del.textContent = 'Delete';
        del.addEventListener('click', async () => {
          if (!confirm('Delete canteen "' + c.name + '"?')) return;
          try {
            const csrf = await getCsrf();
            const headers = {};
            if (csrf.token && csrf.header) headers[csrf.header] = csrf.token;
            const res = await fetch('/api/canteens/' + c.id, { method: 'DELETE', headers });
            if (res.ok) { loadCanteens(); } else { alert('Delete failed: ' + res.status); }
          } catch (err) { console.error('delete failed', err); alert('Delete failed'); }
        });
        // group action buttons with spacing
        const actions = document.createElement('div');
        actions.className = 'btn-row';
        actions.appendChild(a);
        actions.appendChild(editBtn);
        actions.appendChild(del);
        card.appendChild(h);
        card.appendChild(p);
        card.appendChild(actions);
        cards.appendChild(card);
      }
    });
  }

  document.getElementById('refresh').addEventListener('click', loadCanteens);
  document.getElementById('addCanteenForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('name').value;
    const location = document.getElementById('location').value;
    const price = parseFloat(document.getElementById('price').value);
    const csrf = await getCsrf();
    const headers = { 'Content-Type': 'application/json' };
    if (csrf.token && csrf.header) headers[csrf.header] = csrf.token;
    const res = await fetch('/api/canteens', { method: 'POST', headers, body: JSON.stringify({ name, location, defaultPlatePrice: price }) });
    if (res.ok) { alert('Created'); loadCanteens(); } else alert('Failed: ' + res.status);
  });

  loadCanteens();
}

// Food page
if (document.getElementById('purchaseForm')) {
  const path = window.location.pathname.split('/');
  // expected /canteen/{id}/food
  const canteenId = path[2];

  async function refreshInventory() {
    const resInv = await fetch(`/api/canteens/${canteenId}/inventory`);
    if (resInv.ok) {
      const rem = await resInv.json();
      document.getElementById('remaining').textContent = rem;
    }
    const resSales = await fetch('/api/dashboard/stats');
    if (resSales.ok) {
      const stats = await resSales.json();
      document.getElementById('sales').textContent = stats.totalSalesToday;
    }
  }

  document.getElementById('purchaseForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const count = parseInt(document.getElementById('purchaseCount').value || '0', 10);
    const csrf = await getCsrf();
    const headers = { 'Content-Type': 'application/json' };
    if (csrf.token && csrf.header) headers[csrf.header] = csrf.token;
    const res = await fetch(`/api/canteens/${canteenId}/purchases`, { method: 'POST', headers, body: JSON.stringify({ platesBought: count }) });
    if (res.ok) { alert('Purchase recorded'); refreshInventory(); } else alert('Failed: ' + res.status);
  });

  document.getElementById('saleForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const count = parseInt(document.getElementById('saleCount').value || '0', 10);
    const csrf = await getCsrf();
    const headers = { 'Content-Type': 'application/json' };
    if (csrf.token && csrf.header) headers[csrf.header] = csrf.token;
    const res = await fetch(`/api/canteens/${canteenId}/sales`, { method: 'POST', headers, body: JSON.stringify({ platesSold: count }) });
    if (res.ok) { alert('Sale recorded'); refreshInventory(); } else alert('Failed: ' + res.status);
  });

  refreshInventory();
}

// Canteen detail page: show Food / Attendance / Salary buttons
if (document.getElementById('content') && window.location.pathname.match(/^\/canteen\/[^\/]+$/)) {
  (async () => {
    const path = window.location.pathname.split('/');
    const canteenId = path[2];
    try {
      const res = await fetch(`/api/canteens/${canteenId}`);
      if (!res.ok) throw new Error('Failed to load canteen');
      const canteen = await res.json();
      // populate header name if present
      const h = document.querySelector('header h1');
      if (h) h.textContent = canteen.name;

      const content = document.getElementById('content');
      content.innerHTML = '';

      const container = document.createElement('div');
      container.className = 'canteen-options';

      const makeCard = (title, desc, href) => {
        const card = document.createElement('div');
        card.className = 'card option-card';
        const t = document.createElement('h3'); t.textContent = title;
        const p = document.createElement('p'); p.textContent = desc;
        const btn = document.createElement('a'); btn.className = 'btn action'; btn.textContent = title; btn.href = href;
        card.appendChild(t); card.appendChild(p); card.appendChild(btn);
        return card;
      };

      container.appendChild(makeCard('Food Log', 'Manage plates bought/sold and view daily dashboard', `/canteen/${canteenId}/food`));
      container.appendChild(makeCard('Attendance', 'Record worker in/out times and view attendance', `/canteen/${canteenId}/attendance`));
      container.appendChild(makeCard('Salary', 'Mark salaries paid or unpaid and view lists', `/canteen/${canteenId}/salary`));

      content.appendChild(container);
    } catch (err) {
      console.error(err);
      const content = document.getElementById('content');
      if (content) content.textContent = 'Failed to load canteen details.';
    }
  })();
}

// Canteen Food Log Page
if (document.getElementById('food-log-section')) {
  const path = window.location.pathname.split('/');
  const canteenId = path[2];

  const mealTypeSelect = document.getElementById('meal-type');
  const platesProducedInput = document.getElementById('plates-produced');
  const addProducedBtn = document.getElementById('add-produced-btn');
  const platesSoldInput = document.getElementById('plates-sold');
  const addSoldBtn = document.getElementById('add-sold-btn');

  const totalProducedSpan = document.getElementById('total-produced');
  const totalSoldSpan = document.getElementById('total-sold');
  const totalRevenueSpan = document.getElementById('total-revenue');
  const foodLogBody = document.getElementById('food-log-body');

  // Populate canteen name in header if element is present
  (async () => {
    try {
      const res = await fetch(`/api/canteens/${canteenId}`);
      if (res.ok) {
        const canteen = await res.json();
        const nameEl = document.getElementById('page-canteen-name');
        if (nameEl) nameEl.textContent = `— ${canteen.name}`;
      }
    } catch (_) { /* ignore */ }
  })();

  async function fetchFoodLogs() {
    try {
      const response = await fetch(`/api/canteens/${canteenId}/foodlogs`);
      const foodLogs = await response.json();
      renderFoodLogTable(foodLogs);
    } catch (error) {
      console.error('Error fetching food logs:', error);
    }
  }

  async function fetchDailyStats() {
    try {
      const response = await fetch(`/api/canteens/${canteenId}/foodlogs/stats`);
      const stats = await response.json();
      totalProducedSpan.textContent = stats.totalPlatesProduced;
      totalSoldSpan.textContent = stats.totalPlatesSold;
      totalRevenueSpan.textContent = stats.totalSales.toFixed(2);
    } catch (error) {
      console.error('Error fetching daily stats:', error);
    }
  }

  function renderFoodLogTable(foodLogs) {
    foodLogBody.innerHTML = '';
    foodLogs.forEach(log => {
      const mealLabel = (log.mealType === 'NIGHT') ? 'EVENING' : log.mealType;
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>${mealLabel}</td>
        <td>${log.platesProduced}</td>
        <td>${log.platesSold}</td>
      `;
      foodLogBody.appendChild(row);
    });
  }

  async function handleAdd(type) {
    const mealType = mealTypeSelect.value;
    let platesProduced = 0;
    let platesSold = 0;

    if (type === 'produced') {
      platesProduced = parseInt(platesProducedInput.value, 10);
      if (isNaN(platesProduced) || platesProduced <= 0) {
        alert('Please enter a valid number of plates produced.');
        return;
      }
    } else if (type === 'sold') {
      platesSold = parseInt(platesSoldInput.value, 10);
      if (isNaN(platesSold) || platesSold <= 0) {
        alert('Please enter a valid number of plates sold.');
        return;
      }
    }

    const csrf = await getCsrf();
    const headers = { 'Content-Type': 'application/json' };
    if (csrf.token && csrf.header) {
        headers[csrf.header] = csrf.token;
    }

    try {
      const response = await fetch(`/api/canteens/${canteenId}/foodlogs`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ mealType, platesProduced, platesSold }),
      });

      if (response.ok) {
        platesProducedInput.value = '';
        platesSoldInput.value = '';
        fetchFoodLogs();
        fetchDailyStats();
      } else {
        alert('Failed to save food log.');
      }
    } catch (error) {
      console.error('Error saving food log:', error);
    }
  }

  addProducedBtn.addEventListener('click', () => handleAdd('produced'));
  addSoldBtn.addEventListener('click', () => handleAdd('sold'));

  // Quick-add increment buttons for inputs
  document.querySelectorAll('[data-add-target][data-add]').forEach(btn => {
    btn.addEventListener('click', () => {
      const sel = btn.getAttribute('data-add-target');
      const inc = parseInt(btn.getAttribute('data-add') || '0', 10) || 0;
      const input = document.querySelector(sel);
      if (!input) return;
      const cur = parseInt(input.value || '0', 10) || 0;
      const next = Math.max(0, cur + inc);
      input.value = String(next);
      input.focus();
    });
  });

  // Initial load
  fetchFoodLogs();
  fetchDailyStats();
}

// Food Log page (old one, migrated to use canonical API)
if (document.getElementById('food-log-forms')) {
  const path = window.location.pathname.split('/');
  const canteenId = path[2];

  async function getCanteenDetails() {
    try {
      const res = await fetch(`/api/canteens/${canteenId}`);
      if (res.ok) {
        const canteen = await res.json();
        const nameEl = document.getElementById('canteenName');
        if (nameEl) nameEl.textContent = canteen.name;
      }
    } catch (error) {
      console.error('Failed to load canteen details', error);
    }
  }

  function setCurrentDate() {
    const dateEl = document.getElementById('currentDate');
    if (dateEl) dateEl.textContent = new Date().toLocaleDateString();
  }

  async function updateStats() {
    try {
      const res = await fetch(`/api/canteens/${canteenId}/foodlogs/stats`);
      if (res.ok) {
        const stats = await res.json();
        const setText = (id, value) => { const el = document.getElementById(id); if (el) el.textContent = value; };
        setText('totalBought', stats.totalPlatesProduced || 0);
        setText('totalSold', stats.totalPlatesSold || 0);
        setText('remainingPlates', (stats.totalPlatesProduced || 0) - (stats.totalPlatesSold || 0));
        setText('totalSales', (stats.totalSales || 0).toFixed ? stats.totalSales.toFixed(2) : stats.totalSales);

        // Update meal-specific stats if available
        const mealTypes = ['MORNING', 'AFTERNOON', 'EVENING'];
        mealTypes.forEach(meal => {
          const bought = (stats.platesBoughtByMeal && stats.platesBoughtByMeal[meal]) || 0;
          const sold = (stats.platesSoldByMeal && stats.platesSoldByMeal[meal]) || 0;
          const bEl = document.getElementById(`bought-${meal.toLowerCase()}`);
          const sEl = document.getElementById(`sold-${meal.toLowerCase()}`);
          const rEl = document.getElementById(`remaining-${meal.toLowerCase()}`);
          if (bEl) bEl.textContent = bought;
          if (sEl) sEl.textContent = sold;
          if (rEl) rEl.textContent = bought - sold;
        });
      }
    } catch (error) {
      console.error('Failed to update stats', error);
    }
  }

  async function handleTransaction(mealType, transactionType, plates) {
    if (plates <= 0) {
      alert('Please enter a positive number of plates.');
      return;
    }

    const csrf = await getCsrf();
    const headers = { 'Content-Type': 'application/json' };
    if (csrf.token && csrf.header) {
      headers[csrf.header] = csrf.token;
    }

    // New API: POST /api/canteens/{id}/foodlogs with { mealType, platesProduced, platesSold }
    const body = { mealType: mealType };
    if (transactionType === 'purchase') body.platesProduced = plates;
    else body.platesSold = plates;

    try {
      const res = await fetch(`/api/canteens/${canteenId}/foodlogs`, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(body)
      });

      if (res.ok) {
        alert(`Successfully recorded ${transactionType}.`);
        updateStats();
      } else {
        const error = await res.text();
        alert(`Failed to record ${transactionType}: ${error}`);
      }
    } catch (error) {
      console.error(`Failed to record ${transactionType}`, error);
      alert(`An error occurred while recording the ${transactionType}.`);
    }
  }

  function setupForms() {
    const mealTypes = ['morning', 'afternoon', 'evening'];
    mealTypes.forEach(meal => {
      const purchaseForm = document.getElementById(`${meal}-purchase-form`);
      const saleForm = document.getElementById(`${meal}-sale-form`);

      if (purchaseForm) {
        purchaseForm.addEventListener('submit', (e) => {
          e.preventDefault();
          const plates = parseInt(document.getElementById(`${meal}-purchase-plates`).value, 10);
          handleTransaction(meal.toUpperCase(), 'purchase', plates);
          purchaseForm.reset();
        });
      }

      if (saleForm) {
        saleForm.addEventListener('submit', (e) => {
          e.preventDefault();
          const plates = parseInt(document.getElementById(`${meal}-sale-plates`).value, 10);
          handleTransaction(meal.toUpperCase(), 'sale', plates);
          saleForm.reset();
        });
      }
    });
  }

  // Initial setup
  getCanteenDetails();
  setCurrentDate();
  updateStats();
  setupForms();
}
