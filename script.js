let currentUser = null;
let currentUserType = null;

const API_BASE_URL = "https://your-backend-api-url.com";

function updateDateTime() {
    const now = new Date();
    const dateOptions = { year: 'numeric', month: 'short', day: 'numeric' };
    const timeOptions = { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false };
    
    const date = now.toLocaleDateString('en-US', dateOptions);
    const time = now.toLocaleTimeString('en-US', timeOptions);
    
    document.getElementById('dateTime').textContent = `Date: ${date}   Time: ${time}`;
}

setInterval(updateDateTime, 1000);
updateDateTime();

function hideAllMenus() {
    document.querySelectorAll('.menu-section').forEach(el => {
        el.classList.add('hidden');
    });
    document.getElementById('message').classList.add('hidden');
}

function showMessage(text, type = 'info') {
    const messageEl = document.getElementById('message');
    messageEl.textContent = text;
    messageEl.className = `message ${type}`;
    messageEl.classList.remove('hidden');
    
    setTimeout(() => {
        messageEl.classList.add('hidden');
    }, 5000);
}

function updateLogoutHeader(show, userInfo = '') {
    const headerLogout = document.getElementById('headerWithLogout');
    if (show) {
        headerLogout.classList.remove('hidden');
        document.getElementById('userInfo').textContent = userInfo;
    } else {
        headerLogout.classList.add('hidden');
    }
}

function showDoctorMenu() {
    hideAllMenus();
    document.getElementById('doctorMenu').classList.remove('hidden');
}

function showPatientMenu() {
    hideAllMenus();
    document.getElementById('patientMenu').classList.remove('hidden');
}

function showAdminMenu() {
    hideAllMenus();
    document.getElementById('adminMenu').classList.remove('hidden');
}

function backToMain() {
    hideAllMenus();
    updateLogoutHeader(false);
    currentUser = null;
    currentUserType = null;
    document.getElementById('mainMenu').classList.remove('hidden');
}

function exitApp() {
    if (confirm('Are you sure you want to exit?')) {
        showMessage('Thank you for using Doctor Appointment System!', 'success');
        setTimeout(() => {
            window.close();
        }, 2000);
    }
}

function doctorLogin() {
    hideAllMenus();
    document.getElementById('doctorLoginForm').classList.remove('hidden');
}

function doctorRegister() {
    hideAllMenus();
    document.getElementById('doctorRegisterForm').classList.remove('hidden');
}

function handleDoctorLogin(event) {
    event.preventDefault();
    const doctorId = document.getElementById('doctorId').value;
    const password = document.getElementById('doctorPassword').value;
    
    fetch(`${API_BASE_URL}/api/doctors/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ doctorId: doctorId, password: password })
    })
    .then(res => res.json())
    .then(response => {
        if (response.success) {
            currentUser = {
                id: response.doctorId,
                name: response.name,
                specialty: response.specialty,
                type: 'doctor'
            };
            currentUserType = 'doctor';
            showMessage(`Welcome ${response.name}!`, 'success');
            
            setTimeout(() => {
                showDoctorHome();
                document.getElementById('doctorId').value = '';
                document.getElementById('doctorPassword').value = '';
            }, 1500);
        } else {
            showMessage(response.message || 'Invalid credentials', 'error');
        }
    })
    .catch(err => {
        console.error('Error:', err);
        showMessage('Error connecting to server', 'error');
    });
}

function handleDoctorRegister(event) {
    event.preventDefault();
    const doctorId = document.getElementById('newDoctorId').value;
    const name = document.getElementById('doctorName').value;
    const specialty = document.getElementById('doctorSpecialty').value;
    const password = document.getElementById('newDoctorPassword').value;
    
    fetch(`${API_BASE_URL}/api/doctors/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
            doctorId: doctorId, 
            name: name, 
            specialty: specialty, 
            password: password 
        })
    })
    .then(res => res.json())
    .then(response => {
        if (response.success) {
            showMessage(`✅ ${name} registered successfully! Please login.`, 'success');
            setTimeout(() => {
                document.getElementById('newDoctorId').value = '';
                document.getElementById('doctorName').value = '';
                document.getElementById('doctorSpecialty').value = '';
                document.getElementById('newDoctorPassword').value = '';
                showDoctorMenu();
            }, 2000);
        } else {
            showMessage(response.message || 'Registration failed', 'error');
        }
    })
    .catch(err => {
        console.error('Error:', err);
        showMessage('Error connecting to server', 'error');
    });
}

function patientLogin() {
    hideAllMenus();
    document.getElementById('patientLoginForm').classList.remove('hidden');
}

function patientRegister() {
    hideAllMenus();
    document.getElementById('patientRegisterForm').classList.remove('hidden');
}

function handlePatientLogin(event) {
    event.preventDefault();
    const patientId = document.getElementById('patientId').value;
    const password = document.getElementById('patientPassword').value;
    
    fetch(`${API_BASE_URL}/api/patients/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            patientId: patientId,
            password: password 
        })
    })
    .then(res => res.json())
    .then(response => {
        if (response.success) {
            currentUser = {
                id: response.patientId,
                name: response.name,
                type: 'patient'
            };
            currentUserType = 'patient';
            
            showMessage(`Welcome ${response.name}!`, 'success');
            
            setTimeout(() => {
                showPatientHome();
                document.getElementById('patientId').value = '';
                document.getElementById('patientPassword').value = '';
            }, 1000);
        } else {
            showMessage(response.message || 'Invalid credentials', 'error');
        }
    })
    .catch(err => {
        console.error('Error:', err);
        showMessage('Error connecting to server', 'error');
    });
}

function handlePatientRegister(event) {
    event.preventDefault();
    const patientId = document.getElementById('newPatientId').value;
    const name = document.getElementById('patientName').value;
    const age = document.getElementById('patientAge').value;
    const password = document.getElementById('newPatientPassword').value;
    
    fetch(`${API_BASE_URL}/api/patients/register`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            patientId: patientId,
            name: name,
            age: age,
            password: password 
        })
    })
    .then(res => res.json())
    .then(response => {
        if (response.success) {
            showMessage(`✅ ${name} registered successfully! Please login.`, 'success');
            setTimeout(() => {
                document.getElementById('newPatientId').value = '';
                document.getElementById('patientName').value = '';
                document.getElementById('patientAge').value = '';
                document.getElementById('newPatientPassword').value = '';
                showPatientMenu();
            }, 2000);
        } else {
            showMessage(response.message || 'Registration failed', 'error');
        }
    })
    .catch(err => {
        console.error('Error:', err);
        showMessage('Error connecting to server', 'error');
    });
}

function adminLogin() {
    hideAllMenus();
    document.getElementById('adminLoginForm').classList.remove('hidden');
}

function handleAdminLogin(event) {
    event.preventDefault();
    const adminId = document.getElementById('adminId').value;
    const password = document.getElementById('adminPassword').value;
    
    showMessage(`Admin ${adminId} logged in successfully!`, 'success');
    
    setTimeout(() => {
        hideAllMenus();
        document.getElementById('mainMenu').classList.remove('hidden');
        document.getElementById('adminId').value = '';
        document.getElementById('adminPassword').value = '';
    }, 2000);
}

function logout() {
    if (confirm('Are you sure you want to logout?')) {
        currentUser = null;
        currentUserType = null;
        updateLogoutHeader(false);
        hideAllMenus();
        document.getElementById('mainMenu').classList.remove('hidden');
        showMessage('Logged out successfully', 'success');
    }
}

function showPatientHome() {
    hideAllMenus();
    updateLogoutHeader(true, `👤 ${currentUser.name} (Patient)`);
    document.getElementById('patientHome').classList.remove('hidden');
    document.getElementById('patientNameDisplay').textContent = `Welcome, ${currentUser.name}`;
    
    loadDoctorsList();
    loadPatientRequests();
    loadPatientAppointments();
    
    const today = new Date();
    const dateInput = document.getElementById('appointmentDate');
    dateInput.min = today.toISOString().split('T')[0];
    
    const maxDate = new Date(today);
    maxDate.setDate(maxDate.getDate() + 30);
    dateInput.max = maxDate.toISOString().split('T')[0];
}

function loadDoctorsList() {
    fetch(`${API_BASE_URL}/api/doctors/list`)
        .then(res => res.json())
        .then(doctors => {
            const select = document.getElementById('doctorSelect');
            select.innerHTML = '<option value="">-- Choose a Doctor --</option>';
            doctors.forEach(doc => {
                const option = document.createElement('option');
                option.value = doc.id;
                option.textContent = `${doc.name} (${doc.specialty})`;
                option.dataset.name = doc.name;
                option.dataset.specialty = doc.specialty;
                select.appendChild(option);
            });
        })
        .catch(err => console.error('Error loading doctors:', err));
}

function updateDoctorInfo() {
    const select = document.getElementById('doctorSelect');
    const selected = select.options[select.selectedIndex];
    const infoDiv = document.getElementById('doctorInfo');
    
    if (select.value) {
        infoDiv.innerHTML = `
            <p><strong>👨‍⚕️ ${selected.dataset.name}</strong></p>
            <p>🏥 Specialty: ${selected.dataset.specialty}</p>
            <p>⏰ Available: Mon-Fri, 9:00 AM - 5:00 PM</p>
        `;
    } else {
        infoDiv.innerHTML = '';
    }
}

function handleBookAppointment(event) {
    event.preventDefault();
    
    const doctorId = document.getElementById('doctorSelect').value;
    const date = document.getElementById('appointmentDate').value;
    const time = document.getElementById('appointmentTime').value;
    const reason = document.getElementById('appointmentReason').value;
    
    if (!doctorId) {
        showMessage('Please select a doctor', 'error');
        return;
    }
    
    const data = {
        patientId: currentUser.id,
        patientName: currentUser.name,
        doctorId: doctorId,
        date: date,
        time: time,
        reason: reason
    };
    
    fetch(`${API_BASE_URL}/api/appointment/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(response => {
        if (response.success) {
            showMessage('✅ Appointment request sent successfully!', 'success');
            event.currentTarget.reset();
            document.getElementById('doctorInfo').innerHTML = '';
            setTimeout(() => {
                loadPatientRequests();
                switchPatientTab('myRequests');
            }, 1500);
        } else {
            showMessage('Error sending request', 'error');
        }
    })
    .catch(err => {
        console.error('Error:', err);
        showMessage('Error connecting to server', 'error');
    });
}

function loadPatientRequests() {
    fetch(`${API_BASE_URL}/api/appointment/requests/patient?patientId=${currentUser.id}`)
        .then(res => res.json())
        .then(requests => {
            displayPatientRequests(requests);
        })
        .catch(err => {
            console.error('Error loading requests:', err);
            document.getElementById('requestsList').innerHTML = '<p class="empty-message">Error loading requests</p>';
        });
}

function displayPatientRequests(requests) {
    const listDiv = document.getElementById('requestsList');
    
    if (requests.length === 0) {
        listDiv.innerHTML = '<p class="empty-message">No appointment requests yet</p>';
        return;
    }
    
    listDiv.innerHTML = requests.map(req => `
        <div class="appointment-card request">
            <div class="appointment-header">
                <h4>💬 ${req.doctorName}</h4>
                <span class="status-badge status-${req.status.toLowerCase()}">${req.status}</span>
            </div>
            <div class="appointment-details">
                <div class="detail-item">
                    <span class="detail-label">📅 Date:</span>
                    <span class="detail-value">${req.date}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">🕐 Time:</span>
                    <span class="detail-value">${req.time}</span>
                </div>
            </div>
            <div class="appointment-reason">
                <strong>Reason:</strong> ${req.reason}
            </div>
        </div>
    `).join('');
}

function loadPatientAppointments() {
    fetch(`${API_BASE_URL}/api/appointments/patient?patientId=${currentUser.id}`)
        .then(res => res.json())
        .then(appointments => {
            displayPatientAppointments(appointments);
        })
        .catch(err => {
            console.error('Error loading appointments:', err);
            document.getElementById('appointmentsList').innerHTML = '<p class="empty-message">Error loading appointments</p>';
        });
}

function displayPatientAppointments(appointments) {
    const listDiv = document.getElementById('appointmentsList');
    
    if (appointments.length === 0) {
        listDiv.innerHTML = '<p class="empty-message">No confirmed appointments yet</p>';
        return;
    }
    
    listDiv.innerHTML = appointments.map(apt => `
        <div class="appointment-card appointment">
            <div class="appointment-header">
                <h4>✅ ${apt.doctorName}</h4>
                <span class="status-badge status-${apt.status.toLowerCase()}">${apt.status}</span>
            </div>
            <div class="appointment-details">
                <div class="detail-item">
                    <span class="detail-label">📅 Date:</span>
                    <span class="detail-value">${apt.date}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">🕐 Time:</span>
                    <span class="detail-value">${apt.time}</span>
                </div>
            </div>
        </div>
    `).join('');
}

function switchPatientTab(tabName) {
    document.querySelectorAll('#patientHome .tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('#patientHome .tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    document.getElementById(tabName).classList.add('active');
    event.currentTarget.classList.add('active');
    
    if (tabName === 'myRequests') {
        loadPatientRequests();
    } else if (tabName === 'myAppointments') {
        loadPatientAppointments();
    }
}

function showDoctorHome() {
    hideAllMenus();
    updateLogoutHeader(true, `👨‍⚕️ ${currentUser.name} (Doctor)`);
    document.getElementById('doctorHome').classList.remove('hidden');
    document.getElementById('doctorNameDisplay').textContent = currentUser.name;
    
    loadDoctorRequests();
    loadDoctorAppointments();
}

function loadDoctorRequests() {
    fetch(`${API_BASE_URL}/api/appointment/requests/doctor?doctorId=${currentUser.id}`)
        .then(res => res.json())
        .then(requests => {
            displayDoctorRequests(requests);
        })
        .catch(err => {
            console.error('Error loading requests:', err);
            document.getElementById('doctorRequestsList').innerHTML = '<p class="empty-message">Error loading requests</p>';
        });
}

function displayDoctorRequests(requests) {
    const listDiv = document.getElementById('doctorRequestsList');
    
    const pendingRequests = requests.filter(req => req.status.toLowerCase() === 'pending');
    
    if (pendingRequests.length === 0) {
        listDiv.innerHTML = '<p class="empty-message">No pending appointment requests</p>';
        return;
    }
    
    listDiv.innerHTML = pendingRequests.map(req => `
        <div class="appointment-card request">
            <div class="appointment-header">
                <h4>👤 ${req.patientName}</h4>
                <span class="status-badge status-pending">PENDING</span>
            </div>
            <div class="appointment-details">
                <div class="detail-item">
                    <span class="detail-label">📅 Date:</span>
                    <span class="detail-value">${req.date}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">🕐 Time:</span>
                    <span class="detail-value">${req.time}</span>
                </div>
            </div>
            <div class="appointment-reason">
                <strong>Symptoms/Reason:</strong> ${req.reason}
            </div>
            <div class="appointment-actions">
                <button class="btn btn-accept" onclick="acceptAppointmentRequest('${req.id}')">✅ Accept</button>
                <button class="btn btn-reject" onclick="rejectAppointmentRequest('${req.id}')">❌ Reject</button>
            </div>
        </div>
    `).join('');
}

function acceptAppointmentRequest(requestId) {
    fetch(`${API_BASE_URL}/api/appointment/accept`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ requestId: requestId })
    })
    .then(res => res.json())
    .then(response => {
        if (response.success) {
            showMessage('✅ Appointment request accepted!', 'success');
            loadDoctorRequests();
            loadDoctorAppointments();
        } else {
            showMessage('Error accepting request', 'error');
        }
    })
    .catch(err => {
        console.error('Error:', err);
        showMessage('Error connecting to server', 'error');
    });
}

function rejectAppointmentRequest(requestId) {
    if (confirm('Reject this appointment request?')) {
        fetch(`${API_BASE_URL}/api/appointment/reject`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ requestId: requestId })
        })
        .then(res => res.json())
        .then(response => {
            if (response.success) {
                showMessage('❌ Appointment request rejected', 'success');
                loadDoctorRequests();
            } else {
                showMessage('Error rejecting request', 'error');
            }
        })
        .catch(err => {
            console.error('Error:', err);
            showMessage('Error connecting to server', 'error');
        });
    }
}

function loadDoctorAppointments() {
    fetch(`${API_BASE_URL}/api/appointments/doctor?doctorId=${currentUser.id}`)
        .then(res => res.json())
        .then(appointments => {
            displayDoctorAppointments(appointments);
        })
        .catch(err => {
            console.error('Error loading appointments:', err);
            document.getElementById('doctorScheduleList').innerHTML = '<p class="empty-message">Error loading appointments</p>';
        });
}

function displayDoctorAppointments(appointments) {
    const listDiv = document.getElementById('doctorScheduleList');
    
    if (appointments.length === 0) {
        listDiv.innerHTML = '<p class="empty-message">No scheduled appointments</p>';
        return;
    }
    
    listDiv.innerHTML = appointments.map(apt => `
        <div class="appointment-card appointment">
            <div class="appointment-header">
                <h4>👤 ${apt.patientName}</h4>
                <span class="status-badge status-${apt.status.toLowerCase()}">${apt.status}</span>
            </div>
            <div class="appointment-details">
                <div class="detail-item">
                    <span class="detail-label">📅 Date:</span>
                    <span class="detail-value">${apt.date}</span>
                </div>
                <div class="detail-item">
                    <span class="detail-label">🕐 Time:</span>
                    <span class="detail-value">${apt.time}</span>
                </div>
            </div>
        </div>
    `).join('');
}

function switchDoctorTab(tabName) {
    document.querySelectorAll('#doctorHome .tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('#doctorHome .tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    document.getElementById(tabName).classList.add('active');
    event.currentTarget.classList.add('active');
    
    if (tabName === 'requests') {
        loadDoctorRequests();
    } else if (tabName === 'schedule') {
        loadDoctorAppointments();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('mainMenu').classList.remove('hidden');
});
