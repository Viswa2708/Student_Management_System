/* ============================================
   StudentHub - Application JavaScript
   ============================================ */

// ==================== STATE ====================
let authToken = '';
let currentUser = { username: '', role: '', teacherId: null };
let currentPage = 'dashboard';
let currentEditId = null;
let currentModalType = '';

// Cache for data
let studentsCache = [];
let teachersCache = [];
let subjectsCache = [];
let examsCache = [];

// ==================== API HELPERS ====================
const API_BASE = '';

function getHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + authToken
    };
}

async function apiCall(url, method = 'GET', body = null) {
    const options = {
        method,
        headers: getHeaders()
    };
    if (body) options.body = JSON.stringify(body);

    const response = await fetch(API_BASE + url, options);

    if (response.status === 401 || response.status === 403) {
        const text = await response.text();
        let msg = response.status === 401 ? 'Invalid credentials' : 'Access denied';
        try {
            const data = JSON.parse(text);
            msg = data.message || data.error || msg;
        } catch (e) { }
        throw new Error(msg);
    }

    if (!response.ok) {
        const text = await response.text();
        let msg = 'Something went wrong';
        try {
            const data = JSON.parse(text);
            msg = data.message || data.error || msg;
        } catch (e) {
            if (text) msg = text;
        }
        throw new Error(msg);
    }

    if (response.status === 204 || response.headers.get('content-length') === '0') {
        return null;
    }

    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
        return response.json();
    }
    return response.text();
}

// ==================== AUTH ====================
function fillCredentials(username, password) {
    document.getElementById('login-username').value = username;
    document.getElementById('login-password').value = password;
}

document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value.trim();

    if (!username || !password) return;

    const loginBtn = document.getElementById('login-btn');
    const btnText = loginBtn.querySelector('.btn-text');
    const btnLoader = loginBtn.querySelector('.btn-loader');
    const errorDiv = document.getElementById('login-error');

    btnText.style.display = 'none';
    btnLoader.style.display = 'inline-flex';
    errorDiv.style.display = 'none';
    loginBtn.disabled = true;

    try {
        authToken = btoa(username + ':' + password);

        // Use the /api/me endpoint to get proper role
        const meData = await apiCall('/api/me');

        currentUser = {
            username: meData.username,
            displayName: meData.displayName || meData.username,
            role: meData.role,
            teacherId: meData.teacherId || null,
            rollNo: meData.rollNo || null
        };

        sessionStorage.setItem('auth', authToken);
        sessionStorage.setItem('user', JSON.stringify(currentUser));

        showApp();
    } catch (err) {
        errorDiv.textContent = err.message || 'Invalid username or password';
        errorDiv.style.display = 'block';
        authToken = '';
    } finally {
        btnText.style.display = 'inline';
        btnLoader.style.display = 'none';
        loginBtn.disabled = false;
    }
});

function logout() {
    authToken = '';
    currentUser = { username: '', role: '' };
    sessionStorage.clear();
    document.getElementById('app').style.display = 'none';
    document.getElementById('login-page').style.display = 'flex';
    document.getElementById('login-username').value = '';
    document.getElementById('login-password').value = '';
    document.getElementById('login-error').style.display = 'none';
}

// Check for existing session
(function checkSession() {
    const savedAuth = sessionStorage.getItem('auth');
    const savedUser = sessionStorage.getItem('user');
    if (savedAuth && savedUser) {
        authToken = savedAuth;
        currentUser = JSON.parse(savedUser);
        showApp();
    }
})();

// ==================== APP INITIALIZATION ====================
function showApp() {
    document.getElementById('login-page').style.display = 'none';
    document.getElementById('app').style.display = 'flex';

    // Update user info in sidebar
    const displayName = currentUser.displayName || currentUser.username;
    document.getElementById('user-avatar').textContent = displayName.charAt(0).toUpperCase();
    document.getElementById('user-name').textContent = escapeHtml(displayName);
    document.getElementById('user-role').textContent = currentUser.role;

    buildNavMenu();
    navigateTo('dashboard');
}

function buildNavMenu() {
    const menu = document.getElementById('nav-menu');
    let items = [];

    items.push({ icon: '🏠', label: 'Dashboard', page: 'dashboard' });

    if (currentUser.role === 'ADMIN') {
        items.push({ icon: '👨‍🎓', label: 'Students', page: 'students' });
        items.push({ icon: '👩‍🏫', label: 'Teachers', page: 'teachers' });
        items.push({ icon: '📖', label: 'Subjects', page: 'subjects' });
        items.push({ icon: '📋', label: 'Exams', page: 'exams' });
        items.push({ icon: '📈', label: 'Analytics', page: 'analytics' });
    } else if (currentUser.role === 'TEACHER') {
        items.push({ icon: '📋', label: 'Exam Results', page: 'exam-results' });
        items.push({ icon: '📈', label: 'Analytics', page: 'analytics' });
    } else if (currentUser.role === 'STUDENT') {
        items.push({ icon: '📋', label: 'My Exam Results', page: 'my-exam-results' });
    }

    menu.innerHTML = items.map(item => `
        <li>
            <a onclick="navigateTo('${item.page}')" data-page="${item.page}">
                <span class="nav-icon">${item.icon}</span>
                ${item.label}
            </a>
        </li>
    `).join('');
}

// ==================== NAVIGATION ====================
function navigateTo(page) {
    currentPage = page;

    // Update active nav item
    document.querySelectorAll('.nav-menu a').forEach(a => {
        a.classList.toggle('active', a.dataset.page === page);
    });

    // Show page
    document.querySelectorAll('.page').forEach(p => {
        p.classList.remove('active');
    });
    const pageEl = document.getElementById('page-' + page);
    if (pageEl) pageEl.classList.add('active');

    // Update topbar title
    const titles = {
        'dashboard': '🏠 Dashboard',
        'students': '👨‍🎓 Students',
        'teachers': '👩‍🏫 Teachers',
        'subjects': '📖 Subjects',
        'exams': '📋 Schedule Exams',
        'exam-results': '📋 Exam Results',
        'my-exam-results': '📋 My Exam Results',
        'analytics': '📈 Analytics'
    };
    document.getElementById('page-title').textContent = titles[page] || 'Dashboard';

    // Show search for list pages
    const searchBox = document.getElementById('search-box');
    searchBox.style.display = ['students', 'teachers', 'subjects'].includes(page) ? 'block' : 'none';
    document.getElementById('search-input').value = '';

    // Load data for page
    loadPageData(page);

    // Close sidebar on mobile
    document.getElementById('sidebar').classList.remove('open');
}

function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('open');
}

// ==================== DATA LOADING ====================
async function loadPageData(page) {
    try {
        switch (page) {
            case 'dashboard':
                await loadDashboard();
                break;
            case 'students':
                await loadStudents();
                break;
            case 'teachers':
                await loadTeachers();
                break;
            case 'subjects':
                await loadSubjects();
                break;
            case 'exams':
                await loadExams();
                break;
            case 'exam-results':
                await loadTeacherExams();
                break;
            case 'my-exam-results':
                await loadStudentExamResults();
                break;
            case 'analytics':
                await loadAnalytics();
                break;
        }
    } catch (err) {
        console.error('Error loading page:', err);
    }
}

// ==================== DASHBOARD ====================
async function loadDashboard() {
    const statsGrid = document.getElementById('stats-grid');
    const recentStudents = document.getElementById('recent-students');
    const quickStats = document.getElementById('quick-stats');
    const recentSection = document.getElementById('recent-students-section');

    if (currentUser.role === 'ADMIN') {
        recentSection.style.display = 'block';
        try {
            const [students, teachers, subjects] = await Promise.all([
                apiCall('/api/admin/students'),
                apiCall('/api/admin/teachers'),
                apiCall('/api/admin/subjects')
            ]);

            studentsCache = students || [];
            teachersCache = teachers || [];
            subjectsCache = subjects || [];

            statsGrid.innerHTML = `
                <div class="stat-card red">
                    <div class="stat-icon">👨‍🎓</div>
                    <div class="stat-info">
                        <h3>${studentsCache.length}</h3>
                        <p>Total Students</p>
                    </div>
                </div>
                <div class="stat-card blue">
                    <div class="stat-icon">👩‍🏫</div>
                    <div class="stat-info">
                        <h3>${teachersCache.length}</h3>
                        <p>Total Teachers</p>
                    </div>
                </div>
                <div class="stat-card green">
                    <div class="stat-icon">📖</div>
                    <div class="stat-info">
                        <h3>${subjectsCache.length}</h3>
                        <p>Total Subjects</p>
                    </div>
                </div>
                <div class="stat-card purple">
                    <div class="stat-icon">🎓</div>
                    <div class="stat-info">
                        <h3>${new Set(studentsCache.map(s => s.department)).size}</h3>
                        <p>Departments</p>
                    </div>
                </div>
            `;

            // Recent students
            const recent = studentsCache.slice(-5).reverse();
            recentStudents.innerHTML = recent.length ? recent.map(s => `
                <div class="recent-item">
                    <div class="recent-item-avatar">${s.name ? s.name.charAt(0) : '?'}</div>
                    <div class="recent-item-info">
                        <div class="name">${escapeHtml(s.name)}</div>
                        <div class="detail">${escapeHtml(s.email)} • ${escapeHtml(s.department || '')}</div>
                    </div>
                </div>
            `).join('') : '<p style="color:var(--text-muted);padding:20px;">No students yet</p>';

            // Quick stats
            const departments = {};
            studentsCache.forEach(s => {
                departments[s.department] = (departments[s.department] || 0) + 1;
            });
            quickStats.innerHTML = `
                <div class="quick-stat-item">
                    <span class="label">Students</span>
                    <span class="value">${studentsCache.length}</span>
                </div>
                <div class="quick-stat-item">
                    <span class="label">Teachers</span>
                    <span class="value">${teachersCache.length}</span>
                </div>
                <div class="quick-stat-item">
                    <span class="label">Subjects</span>
                    <span class="value">${subjectsCache.length}</span>
                </div>
                ${Object.entries(departments).map(([dept, count]) => `
                    <div class="quick-stat-item">
                        <span class="label">${escapeHtml(dept)}</span>
                        <span class="value">${count}</span>
                    </div>
                `).join('')}
            `;
        } catch (err) {
            statsGrid.innerHTML = '<p style="color:var(--text-muted);">Unable to load dashboard data</p>';
        }
    } else if (currentUser.role === 'TEACHER') {
        recentSection.style.display = 'none';
        statsGrid.innerHTML = `
            <div class="stat-card blue">
                <div class="stat-icon">👩‍🏫</div>
                <div class="stat-info">
                    <h3>Teacher</h3>
                    <p>Welcome, ${escapeHtml(currentUser.displayName || currentUser.username)}</p>
                </div>
            </div>
            <div class="stat-card green">
                <div class="stat-icon">📝</div>
                <div class="stat-info">
                    <h3>Marks</h3>
                    <p>Go to Manage Marks</p>
                </div>
            </div>
        `;
        quickStats.innerHTML = `
            <div class="quick-stat-item">
                <span class="label">Role</span>
                <span class="value">Teacher</span>
            </div>
            <div class="quick-stat-item">
                <span class="label">Tip</span>
                <span class="value" style="font-size:12px;">Use sidebar to manage marks</span>
            </div>
        `;
    } else if (currentUser.role === 'STUDENT') {
        recentSection.style.display = 'none';
        statsGrid.innerHTML = `
            <div class="stat-card green">
                <div class="stat-icon">🎒</div>
                <div class="stat-info">
                    <h3>Student</h3>
                    <p>Welcome, ${escapeHtml(currentUser.displayName || currentUser.username)}</p>
                </div>
            </div>
            <div class="stat-card purple">
                <div class="stat-icon">📊</div>
                <div class="stat-info">
                    <h3>Marks</h3>
                    <p>View from sidebar</p>
                </div>
            </div>
        `;
        quickStats.innerHTML = `
            <div class="quick-stat-item">
                <span class="label">Role</span>
                <span class="value">Student</span>
            </div>
            <div class="quick-stat-item">
                <span class="label">Tip</span>
                <span class="value" style="font-size:12px;">Go to My Marks in sidebar</span>
            </div>
        `;
    }
}

// ==================== STUDENTS CRUD ====================
async function loadStudents() {
    try {
        // Load both students AND subjects so enrollment checkboxes work in the modal
        [studentsCache, subjectsCache] = await Promise.all([
            apiCall('/api/admin/students'),
            apiCall('/api/admin/subjects')
        ]);
        studentsCache = studentsCache || [];
        subjectsCache = subjectsCache || [];
        renderStudents(studentsCache);
    } catch (err) {
        showToast('Failed to load students: ' + err.message, 'error');
    }
}

function renderStudents(students) {
    const tbody = document.getElementById('students-tbody');
    const empty = document.getElementById('students-empty');
    const table = document.getElementById('students-table');

    if (!students.length) {
        table.style.display = 'none';
        empty.style.display = 'block';
        return;
    }

    table.style.display = 'table';
    empty.style.display = 'none';

    tbody.innerHTML = students.map(s => `
        <tr>
            <td>${escapeHtml(s.rollNo)}</td>
            <td>
                <div style="display:flex;align-items:center;gap:10px;">
                    <div class="recent-item-avatar" style="width:30px;height:30px;font-size:12px;">${s.name ? s.name.charAt(0) : '?'}</div>
                    ${escapeHtml(s.name)}
                </div>
            </td>
            <td>${escapeHtml(s.email)}</td>
            <td><span class="badge dept">${escapeHtml(s.department || '-')}</span></td>
            <td><span class="badge year">Year ${s.year || '-'}</span></td>
            <td>
                <div class="actions-cell">
                    <button class="btn-icon edit" title="Edit" onclick="editStudent('${s.rollNo}')">✏️</button>
                    <button class="btn-icon delete" title="Delete" onclick="deleteStudent('${s.rollNo}')">🗑️</button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function editStudent(rollNo) {
    try {
        // Fetch fresh student data from server to get accurate subjectIds
        const student = await apiCall(`/api/admin/students/${rollNo}`);
        if (student) {
            currentEditId = rollNo;
            showModal('student', student);
        }
    } catch (err) {
        showToast('Failed to load student details: ' + err.message, 'error');
    }
}

async function deleteStudent(rollNo) {
    if (!confirm('Are you sure you want to delete this student?')) return;
    try {
        await apiCall(`/api/admin/students/${rollNo}`, 'DELETE');
        showToast('Student deleted successfully', 'success');
        await loadStudents();
    } catch (err) {
        showToast('Failed to delete student: ' + err.message, 'error');
    }
}

// ==================== TEACHERS CRUD ====================
async function loadTeachers() {
    try {
        teachersCache = await apiCall('/api/admin/teachers') || [];
        renderTeachers(teachersCache);
    } catch (err) {
        showToast('Failed to load teachers: ' + err.message, 'error');
    }
}

function renderTeachers(teachers) {
    const tbody = document.getElementById('teachers-tbody');
    const empty = document.getElementById('teachers-empty');
    const table = document.getElementById('teachers-table');

    if (!teachers.length) {
        table.style.display = 'none';
        empty.style.display = 'block';
        return;
    }

    table.style.display = 'table';
    empty.style.display = 'none';

    tbody.innerHTML = teachers.map(t => `
        <tr>
            <td>${t.id}</td>
            <td>
                <div style="display:flex;align-items:center;gap:10px;">
                    <div class="recent-item-avatar" style="width:30px;height:30px;font-size:12px;background:linear-gradient(135deg, var(--blue), var(--cyan));">${t.name ? t.name.charAt(0) : '?'}</div>
                    ${escapeHtml(t.name)}
                </div>
            </td>
            <td>${escapeHtml(t.email)}</td>
            <td>${t.subjectIds ? t.subjectIds.length + ' subject(s)' : '0 subjects'}</td>
            <td>
                <div class="actions-cell">
                    <button class="btn-icon edit" title="Edit" onclick="editTeacher(${t.id})">✏️</button>
                    <button class="btn-icon delete" title="Delete" onclick="deleteTeacher(${t.id})">🗑️</button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function editTeacher(id) {
    const teacher = teachersCache.find(t => t.id === id);
    if (teacher) {
        currentEditId = id;
        showModal('teacher', teacher);
    }
}

async function deleteTeacher(id) {
    if (!confirm('Are you sure you want to delete this teacher?')) return;
    try {
        await apiCall(`/api/admin/teachers/${id}`, 'DELETE');
        showToast('Teacher deleted successfully', 'success');
        await loadTeachers();
    } catch (err) {
        showToast('Failed to delete teacher: ' + err.message, 'error');
    }
}

// ==================== SUBJECTS CRUD ====================
async function loadSubjects() {
    try {
        subjectsCache = await apiCall('/api/admin/subjects') || [];
        teachersCache = await apiCall('/api/admin/teachers') || [];
        renderSubjects(subjectsCache);
    } catch (err) {
        showToast('Failed to load subjects: ' + err.message, 'error');
    }
}

function renderSubjects(subjects) {
    const tbody = document.getElementById('subjects-tbody');
    const empty = document.getElementById('subjects-empty');
    const table = document.getElementById('subjects-table');

    if (!subjects.length) {
        table.style.display = 'none';
        empty.style.display = 'block';
        return;
    }

    table.style.display = 'table';
    empty.style.display = 'none';

    tbody.innerHTML = subjects.map(s => {
        const teacher = teachersCache.find(t => t.id === s.assignedTeacherId);
        return `
        <tr>
            <td>${s.id}</td>
            <td>${escapeHtml(s.subjectName)}</td>
            <td><span class="badge dept">${escapeHtml(s.subjectCode)}</span></td>
            <td><span class="badge year">Year ${s.year}</span></td>
            <td>${teacher ? escapeHtml(teacher.name) : '<span style="color:var(--text-muted)">Not assigned</span>'}</td>
            <td>
                <div class="actions-cell">
                    <button class="btn-icon edit" title="Edit" onclick="editSubject(${s.id})">✏️</button>
                    <button class="btn-icon delete" title="Delete" onclick="deleteSubject(${s.id})">🗑️</button>
                </div>
            </td>
        </tr>
    `}).join('');
}

async function editSubject(id) {
    const subject = subjectsCache.find(s => s.id === id);
    if (subject) {
        currentEditId = id;
        showModal('subject', subject);
    }
}

async function deleteSubject(id) {
    if (!confirm('Are you sure you want to delete this subject?')) return;
    try {
        await apiCall(`/api/admin/subjects/${id}`, 'DELETE');
        showToast('Subject deleted successfully', 'success');
        await loadSubjects();
    } catch (err) {
        showToast('Failed to delete subject: ' + err.message, 'error');
    }
}

// ==================== EXAMS CRUD (ADMIN) ====================

async function loadExams() {
    try {
        examsCache = await apiCall('/api/admin/exams') || [];
        subjectsCache = await apiCall('/api/admin/subjects') || [];
        renderExams(examsCache);
    } catch (err) {
        showToast('Failed to load exams: ' + err.message, 'error');
    }
}

function renderExams(exams) {
    const tbody = document.getElementById('exams-tbody');
    const empty = document.getElementById('exams-empty');
    const table = document.getElementById('exams-table');

    if (!exams.length) {
        table.style.display = 'none';
        empty.style.display = 'block';
        return;
    }

    table.style.display = 'table';
    empty.style.display = 'none';

    const typeLabels = {
        'CYCLE_TEST': '🔄 Cycle Test',
        'INTERNAL': '📝 Internal',
        'SEMESTER': '🎓 Semester'
    };

    const typeBadgeClass = {
        'CYCLE_TEST': 'badge-cycle',
        'INTERNAL': 'badge-internal',
        'SEMESTER': 'badge-semester'
    };

    tbody.innerHTML = exams.map(e => {
        const progress = e.totalStudents > 0 ? Math.round((e.resultsEntered / e.totalStudents) * 100) : 0;
        return `
        <tr>
            <td>${e.id}</td>
            <td>
                <div style="display:flex;align-items:center;gap:10px;">
                    <div class="recent-item-avatar" style="width:30px;height:30px;font-size:12px;background:linear-gradient(135deg, var(--purple), var(--pink));">
                        ${e.examName ? e.examName.charAt(0) : '?'}
                    </div>
                    ${escapeHtml(e.examName)}
                </div>
            </td>
            <td><span class="badge ${typeBadgeClass[e.examType] || 'dept'}">${typeLabels[e.examType] || e.examType}</span></td>
            <td>${escapeHtml(e.subjectName || '')} <span style="color:var(--text-muted);font-size:12px;">(${escapeHtml(e.subjectCode || '')})</span></td>
            <td><span class="badge year">Year ${e.targetYear}</span></td>
            <td><strong>${e.maxMarks}</strong></td>
            <td>${e.examDate || '<span style="color:var(--text-muted)">Not set</span>'}</td>
            <td>
                <div style="display:flex;align-items:center;gap:8px;">
                    <div class="bar-track" style="width:60px;height:6px;">
                        <div class="bar-fill ${progress === 100 ? 'green' : ''}" style="width:${progress}%"></div>
                    </div>
                    <span style="font-size:12px;color:var(--text-muted);">${e.resultsEntered || 0}/${e.totalStudents || 0}</span>
                </div>
            </td>
            <td>
                <div class="actions-cell">
                    <button class="btn-icon edit" title="Edit" onclick="editExam(${e.id})">✏️</button>
                    <button class="btn-icon delete" title="Delete" onclick="deleteExam(${e.id})">🗑️</button>
                </div>
            </td>
        </tr>
    `}).join('');
}

async function editExam(id) {
    const exam = examsCache.find(e => e.id === id);
    if (exam) {
        currentEditId = id;
        showModal('exam', exam);
    }
}

async function deleteExam(id) {
    if (!confirm('Are you sure you want to delete this exam and all its results?')) return;
    try {
        await apiCall(`/api/admin/exams/${id}`, 'DELETE');
        showToast('Exam deleted successfully', 'success');
        await loadExams();
    } catch (err) {
        showToast('Failed to delete exam: ' + err.message, 'error');
    }
}

// ==================== EXAM RESULTS (TEACHER) ====================
async function loadTeacherExams() {
    const container = document.getElementById('teacher-exam-list');
    container.innerHTML = '<p style="color:var(--text-muted);padding:20px;text-align:center;">Loading exams...</p>';

    try {
        let teacherExams = [];

        if (currentUser.teacherId) {
            // Use the teacher ID from the logged-in user's session
            teacherExams = await apiCall(`/api/teacher/${currentUser.teacherId}/exams`) || [];
        } else {
            // Fallback: iterate through teacher IDs to find matching ones
            for (let i = 1; i <= 20; i++) {
                try {
                    const exams = await apiCall(`/api/teacher/${i}/exams`) || [];
                    if (exams.length > 0) {
                        teacherExams = exams;
                        break;
                    }
                } catch (e2) { }
            }
        }

        if (!teacherExams.length) {
            container.innerHTML = `
                <div class="analytics-card" style="text-align:center;">
                    <span style="font-size:48px;">📋</span>
                    <h4 style="margin-top:12px;">No Exams Assigned</h4>
                    <p style="color:var(--text-muted);margin-top:8px;">
                        No exams have been scheduled for your subjects yet. The admin will schedule exams and they will appear here.
                    </p>
                </div>
            `;
            return;
        }

        // Group exams by subject
        const groupedExams = {};
        teacherExams.forEach(exam => {
            const subjectKey = exam.subjectName || 'Other';
            if (!groupedExams[subjectKey]) groupedExams[subjectKey] = [];
            groupedExams[subjectKey].push(exam);
        });

        const typeLabels = {
            'CYCLE_TEST': '🔄 Cycle Test',
            'INTERNAL': '📝 Internal',
            'SEMESTER': '🎓 Semester'
        };

        let html = '';
        for (const [subjectName, exams] of Object.entries(groupedExams)) {
            const subjectCode = exams[0].subjectCode || '';
            html += `
                <div class="course-section" style="margin-bottom: 32px;">
                    <h3 style="margin-bottom: 16px; border-left: 4px solid var(--primary); padding-left: 12px; font-size: 1.25rem;">
                        📖 Course: ${escapeHtml(subjectName)} ${subjectCode ? `<span style="color:var(--text-muted); font-size: 0.9rem; font-weight: normal; margin-left: 8px;">(${escapeHtml(subjectCode)})</span>` : ''}
                    </h3>
                    <div class="exam-cards-grid">
                        ${exams.map(exam => {
                const progress = exam.totalStudents > 0 ? Math.round((exam.resultsEntered / exam.totalStudents) * 100) : 0;
                return `
                            <div class="exam-card" onclick="openExamResultsModal(${exam.id})">
                                <div class="exam-card-header">
                                    <span class="exam-type-badge ${exam.examType.toLowerCase()}">${typeLabels[exam.examType] || exam.examType}</span>
                                    <span class="exam-max-marks">${exam.maxMarks} marks</span>
                                </div>
                                <h4 class="exam-card-title">${escapeHtml(exam.examName)}</h4>
                                <div class="exam-card-meta">
                                    <span>📅 ${exam.examDate || 'No date'}</span>
                                    <span>🎓 Year ${exam.targetYear}</span>
                                </div>
                                <div class="exam-progress">
                                    <div class="exam-progress-label">
                                        <span>Results: ${exam.resultsEntered || 0} / ${exam.totalStudents || 0}</span>
                                        <span>${progress}%</span>
                                    </div>
                                    <div class="bar-track">
                                        <div class="bar-fill ${progress === 100 ? 'green' : ''}" style="width:${progress}%"></div>
                                    </div>
                                </div>
                                <button class="btn-primary" style="width:100%;margin-top:12px;" onclick="event.stopPropagation(); openExamResultsModal(${exam.id})">
                                    ✏️ Enter/Update Results
                                </button>
                            </div>
                        `;
            }).join('')}
                    </div>
                </div>
            `;
        }
        container.innerHTML = html;
    } catch (err) {
        container.innerHTML = `<p style="color:#ff6b6b;padding:20px;">${escapeHtml(err.message)}</p>`;
    }
}

async function openExamResultsModal(examId) {
    const overlay = document.getElementById('modal-overlay');
    const title = document.getElementById('modal-title');
    const body = document.getElementById('modal-body');

    title.textContent = 'Enter Exam Results';
    body.innerHTML = '<p style="color:var(--text-muted);text-align:center;padding:20px;">Loading students...</p>';
    overlay.style.display = 'flex';

    currentModalType = 'exam-results-bulk';

    try {
        const students = await apiCall(`/api/teacher/exams/${examId}/students`);

        if (!students || !students.length) {
            body.innerHTML = `
                <div style="text-align:center;padding:20px;">
                    <p style="color:var(--text-muted);">No eligible students found for this exam.</p>
                    <p style="color:var(--text-muted);font-size:13px;margin-top:8px;">
                        Students must match the target year of this exam.
                    </p>
                </div>
            `;
            return;
        }

        body.innerHTML = `
            <div style="margin-bottom:12px;padding:12px;background:var(--bg-input);border-radius:var(--radius-sm);">
                <strong>${escapeHtml(students[0].examName)}</strong> — ${escapeHtml(students[0].subjectName)}
                <br><span style="color:var(--text-muted);font-size:13px;">Max marks: ${students[0].maxMarks}</span>
            </div>
            <div style="max-height:400px;overflow-y:auto;">
                <table class="data-table" style="font-size:14px;">
                    <thead>
                        <tr>
                            <th>Roll No</th>
                            <th>Name</th>
                            <th>Marks (/${students[0].maxMarks})</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${students.map((s, i) => `
                            <tr>
                                <td>${escapeHtml(s.studentRollNo)}</td>
                                <td>${escapeHtml(s.studentName)}</td>
                                <td>
                                    <input type="number" class="exam-result-input" data-exam-id="${s.examId}" data-student-id="${s.studentId}" data-student-roll-no="${s.studentRollNo}"
                                           value="${s.marksObtained !== null && s.marksObtained !== undefined ? s.marksObtained : ''}"
                                           min="0" max="${students[0].maxMarks}"
                                           style="width:80px;padding:8px;background:var(--bg-input);border:1px solid var(--border);border-radius:var(--radius-sm);color:var(--text-primary);font-family:var(--font);">
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    } catch (err) {
        body.innerHTML = `<p style="color:#ff6b6b;padding:20px;">${escapeHtml(err.message)}</p>`;
    }
}

// ==================== STUDENT EXAM RESULTS ====================
async function loadStudentExamResults() {
    const container = document.getElementById('student-exam-results');
    container.innerHTML = `<p style="color:var(--text-muted);padding:20px;text-align:center;">Loading your exam results...</p>`;

    try {
        const results = await apiCall(`/api/student/${encodeURIComponent(currentUser.rollNo)}/exam-results`);
        renderStudentResultsTable(container, results);
    } catch (err) {
        container.innerHTML = `
            <div style="padding:20px;">
                <div class="perf-summary" style="flex-direction:column;align-items:center;gap:16px;margin-bottom:20px;">
                    <h3 style="font-size:18px;">📋 My Exam Results</h3>
                    <p style="color:var(--text-secondary);font-size:14px;">Enter your Roll Number to view results.</p>
                    <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap;justify-content:center;">
                        <input type="text" id="exam-student-rollno-input" placeholder="Enter Roll No (e.g., S101)"
                               style="padding:12px 16px;background:var(--bg-input);border:2px solid var(--border);
                               border-radius:var(--radius-sm);color:var(--text-primary);font-size:15px;width:250px;
                               outline:none;font-family:var(--font);" onkeydown="if(event.key==='Enter')fetchStudentExamResults()">
                        <button class="btn-primary" onclick="fetchStudentExamResults()" style="padding:12px 24px;">
                            🔍 View Results
                        </button>
                    </div>
                </div>
                <div id="exam-results-display"></div>
            </div>
        `;
    }
}

async function fetchStudentExamResults() {
    const rollNo = document.getElementById('exam-student-rollno-input')?.value?.trim();
    const resultsDiv = document.getElementById('exam-results-display');
    if (!rollNo) { showToast('Please enter your Roll Number', 'error'); return; }
    resultsDiv.innerHTML = '<p style="color:var(--text-muted);padding:20px;text-align:center;">Loading...</p>';
    try {
        const results = await apiCall(`/api/student/${encodeURIComponent(rollNo)}/exam-results`);
        renderStudentResultsTable(resultsDiv, results);
    } catch (err) {
        resultsDiv.innerHTML = `<p style="color:#ff6b6b;padding:20px;">${escapeHtml(err.message)}</p>`;
    }
}

function getGrade(pct) {
    if (pct >= 90) return 'O';
    if (pct >= 80) return 'A+';
    if (pct >= 70) return 'A';
    if (pct >= 60) return 'B+';
    if (pct >= 50) return 'B';
    if (pct >= 40) return 'C';
    return 'F';
}

function getGradeColor(pct) {
    if (pct >= 75) return '#4ade80';
    if (pct >= 40) return '#facc15';
    return '#f87171';
}

function renderStudentResultsTable(container, results) {
    if (!results || !results.length) {
        container.innerHTML = `
            <div class="perf-summary" style="margin-top:20px;justify-content:center;">
                <div class="perf-summary-item">
                    <div class="label">Status</div>
                    <div class="value" style="font-size:16px;">No exam results found yet.</div>
                </div>
            </div>`;
        return;
    }

    // Group results by subject
    const bySubject = {};
    results.forEach(r => {
        const key = r.subjectCode || r.subjectName;
        if (!bySubject[key]) bySubject[key] = { subjectName: r.subjectName, subjectCode: r.subjectCode, exams: [] };
        bySubject[key].exams.push(r);
    });

    // Summary stats
    const validResults = results.filter(r => r.marksObtained !== null && r.marksObtained !== undefined);
    const avgPct = validResults.length
        ? validResults.reduce((s, r) => s + (r.marksObtained / r.maxMarks * 100), 0) / validResults.length : 0;
    const passed = validResults.filter(r => (r.marksObtained / r.maxMarks * 100) >= 40).length;

    let html = `
        <div style="margin-bottom:20px;display:flex;gap:16px;flex-wrap:wrap;">
            <div class="analytics-card" style="flex:1;min-width:130px;text-align:center;padding:16px;">
                <div style="font-size:28px;font-weight:700;color:var(--accent-primary);">${avgPct.toFixed(1)}%</div>
                <div style="font-size:13px;color:var(--text-muted);margin-top:4px;">Overall Average</div>
            </div>
            <div class="analytics-card" style="flex:1;min-width:130px;text-align:center;padding:16px;">
                <div style="font-size:28px;font-weight:700;color:#4ade80;">${passed}</div>
                <div style="font-size:13px;color:var(--text-muted);margin-top:4px;">Passed</div>
            </div>
            <div class="analytics-card" style="flex:1;min-width:130px;text-align:center;padding:16px;">
                <div style="font-size:28px;font-weight:700;color:#f87171;">${validResults.length - passed}</div>
                <div style="font-size:13px;color:var(--text-muted);margin-top:4px;">Failed</div>
            </div>
            <div class="analytics-card" style="flex:1;min-width:130px;text-align:center;padding:16px;">
                <div style="font-size:28px;font-weight:700;color:#a78bfa;">${Object.keys(bySubject).length}</div>
                <div style="font-size:13px;color:var(--text-muted);margin-top:4px;">Subjects</div>
            </div>
        </div>
        <div style="overflow-x:auto;border-radius:12px;border:1px solid var(--border);">
            <table class="data-table" style="font-size:14px;border-collapse:collapse;width:100%;">
                <thead>
                    <tr style="background:var(--bg-card);">
                        <th style="text-align:left;padding:12px 16px;border-bottom:2px solid var(--border);min-width:160px;">Subject Name</th>
                        <th style="text-align:left;padding:12px 16px;border-bottom:2px solid var(--border);min-width:110px;">Subject Code</th>
                        <th style="text-align:left;padding:12px 16px;border-bottom:2px solid var(--border);min-width:180px;">Exam Name</th>
                        <th style="text-align:center;padding:12px 16px;border-bottom:2px solid var(--border);">Marks Obtained</th>
                        <th style="text-align:center;padding:12px 16px;border-bottom:2px solid var(--border);">Maximum Marks</th>
                        <th style="text-align:center;padding:12px 16px;border-bottom:2px solid var(--border);">Grade</th>
                        <th style="text-align:center;padding:12px 16px;border-bottom:2px solid var(--border);">Exam Type</th>
                    </tr>
                </thead>
                <tbody>
    `;

    const examTypeLabel = { 'CYCLE_TEST': 'Cycle Test', 'INTERNAL': 'Internal', 'SEMESTER': 'Semester' };
    let rowIndex = 0;

    for (const [, subject] of Object.entries(bySubject)) {
        subject.exams.forEach((r, i) => {
            const pct = (r.marksObtained !== null && r.marksObtained !== undefined)
                ? (r.marksObtained / r.maxMarks * 100) : null;
            const grade = pct !== null ? getGrade(pct) : '---';
            const gradeColor = pct !== null ? getGradeColor(pct) : 'var(--text-muted)';
            const marks = (r.marksObtained !== null && r.marksObtained !== undefined) ? r.marksObtained : '---';
            const isFirst = i === 0;
            const rowspan = subject.exams.length;
            const rowBg = rowIndex % 2 === 0 ? 'rgba(255,255,255,0.02)' : 'transparent';

            html += `<tr style="background:${rowBg};">`;
            if (isFirst) {
                html += `
                    <td rowspan="${rowspan}" style="padding:12px 16px;border-right:1px solid var(--border);font-weight:600;vertical-align:middle;border-bottom:2px solid var(--border);">
                        ${escapeHtml(subject.subjectName || '—')}
                    </td>
                    <td rowspan="${rowspan}" style="padding:12px 16px;border-right:1px solid var(--border);font-family:monospace;font-size:13px;color:var(--accent-primary);vertical-align:middle;border-bottom:2px solid var(--border);">
                        ${escapeHtml(subject.subjectCode || '—')}
                    </td>`;
                rowIndex++;
            }
            html += `
                <td style="padding:10px 16px;border-bottom:1px solid var(--border);color:var(--text-secondary);">${escapeHtml(r.examName)}</td>
                <td style="padding:10px 16px;border-bottom:1px solid var(--border);text-align:center;font-weight:700;color:${gradeColor};">${marks}</td>
                <td style="padding:10px 16px;border-bottom:1px solid var(--border);text-align:center;color:var(--text-muted);">${r.maxMarks}</td>
                <td style="padding:10px 16px;border-bottom:1px solid var(--border);text-align:center;"><span style="font-weight:700;color:${gradeColor};">${grade}</span></td>
                <td style="padding:10px 16px;border-bottom:1px solid var(--border);text-align:center;">
                    <span style="background:var(--bg-input);padding:3px 10px;border-radius:20px;font-size:12px;color:var(--accent-primary);">
                        ${examTypeLabel[r.examType] || r.examType || '—'}
                    </span>
                </td>
            </tr>`;
        });
    }

    html += `</tbody></table></div>`;
    container.innerHTML = html;
}


// ==================== ANALYTICS ====================
async function loadAnalytics() {
    const grid = document.getElementById('analytics-grid');
    grid.innerHTML = '<p style="color:var(--text-muted);padding:20px;">Loading analytics...</p>';

    try {
        if (currentUser.role === 'ADMIN') {
            let subjectPerf = [], teacherPerf = [], topStudents = [];

            try { subjectPerf = await apiCall('/api/admin/analytics/subjects') || []; } catch (e) { }
            try { teacherPerf = await apiCall('/api/admin/analytics/teachers') || []; } catch (e) { }
            try { topStudents = await apiCall('/api/admin/analytics/top-students') || []; } catch (e) { }

            let html = '';

            // Subject Performance
            if (subjectPerf.length) {
                html += `
                    <div class="analytics-card">
                        <h4>📖 Subject Performance</h4>
                        ${subjectPerf.map(s => `
                            <div class="analytics-bar">
                                <div class="bar-label">
                                    <span class="name">${escapeHtml(s.subjectName || 'Subject ' + s.subjectId)}</span>
                                    <span class="value">${s.averageMarks ? s.averageMarks.toFixed(1) : '0'}%</span>
                                </div>
                                <div class="bar-track">
                                    <div class="bar-fill" style="width:${s.averageMarks || 0}%"></div>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                `;
            }

            // Teacher Performance
            if (teacherPerf.length) {
                html += `
                    <div class="analytics-card">
                        <h4>👩‍🏫 Teacher Performance</h4>
                        ${teacherPerf.map(t => `
                            <div class="analytics-bar">
                                <div class="bar-label">
                                    <span class="name">${escapeHtml(t.teacherName || 'Teacher ' + t.teacherId)}</span>
                                    <span class="value">${t.passPercentage ? t.passPercentage.toFixed(0) : '0'}% pass</span>
                                </div>
                                <div class="bar-track">
                                    <div class="bar-fill green" style="width:${t.passPercentage || 0}%"></div>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                `;
            }

            // Top Students Grouped by Year
            if (topStudents.length) {
                // Group students by year
                const studentsByYear = {};
                topStudents.forEach(s => {
                    const y = s.year || 'Unknown';
                    if (!studentsByYear[y]) studentsByYear[y] = [];
                    studentsByYear[y].push(s);
                });

                // Sort years and create cards
                const sortedYears = Object.keys(studentsByYear).sort();

                sortedYears.forEach(year => {
                    const yearStudents = studentsByYear[year].slice(0, 5); // Top 5 per year
                    if (yearStudents.length > 0) {
                        html += `
                            <div class="analytics-card">
                                <h4>🏆 Top Students - ${year}${year == 1 ? 'st' : year == 2 ? 'nd' : year == 3 ? 'rd' : 'th'} Year</h4>
                                ${yearStudents.map((s, i) => `
                                    <div class="analytics-bar">
                                        <div class="bar-label">
                                            <span class="name">${i === 0 ? '🥇' : i === 1 ? '🥈' : i === 2 ? '🥉' : `#${i + 1}`} ${escapeHtml(s.studentName || 'Student ' + s.studentId)}</span>
                                            <span class="value">${s.averageMarks ? s.averageMarks.toFixed(1) : '0'}</span>
                                        </div>
                                        <div class="bar-track">
                                            <div class="bar-fill blue" style="width:${s.averageMarks || 0}%"></div>
                                        </div>
                                    </div>
                                `).join('')}
                            </div>
                        `;
                    }
                });
            }

            if (!html) {
                html = `
                    <div class="analytics-card" style="grid-column:1/-1;">
                        <h4>📊 No Data Yet</h4>
                        <p style="color:var(--text-muted);margin-top:8px;">
                            Add students, subjects, and marks to see analytics here.
                        </p>
                    </div>
                `;
            }

            grid.innerHTML = html;
        } else if (currentUser.role === 'TEACHER') {
            // Teachers can see subject performance for their assigned subjects
            let subjectPerf = [];
            const tid = currentUser.teacherId || 1;
            try { subjectPerf = await apiCall(`/api/teacher/${tid}/subjects/performance`) || []; } catch (e) { }

            if (subjectPerf.length) {
                grid.innerHTML = `
                    <div class="analytics-card">
                        <h4>📖 Subject Performance</h4>
                        ${subjectPerf.map(s => `
                            <div class="analytics-bar">
                                <div class="bar-label">
                                    <span class="name">${escapeHtml(s.subjectName || 'Subject ' + s.subjectId)}</span>
                                    <span class="value">${s.averageMarks ? s.averageMarks.toFixed(1) : '0'}%</span>
                                </div>
                                <div class="bar-track">
                                    <div class="bar-fill" style="width:${s.averageMarks || 0}%"></div>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                `;
            } else {
                grid.innerHTML = `
                    <div class="analytics-card" style="grid-column:1/-1;">
                        <h4>📊 No Data Yet</h4>
                        <p style="color:var(--text-muted);margin-top:8px;">
                            Enter results for scheduled exams to see performance analytics.
                        </p>
                    </div>
                `;
            }
        } else {
            grid.innerHTML = '<p style="color:var(--text-muted);padding:40px;text-align:center;">View your results from the My Exam Results page.</p>';
        }
    } catch (err) {
        grid.innerHTML = `<p style="color:var(--text-muted);padding:20px;">Failed to load analytics: ${escapeHtml(err.message)}</p>`;
    }
}

// ==================== MODAL ====================
function showModal(type, data = null) {
    currentModalType = type;
    if (!data) currentEditId = null;

    const overlay = document.getElementById('modal-overlay');
    const title = document.getElementById('modal-title');
    const body = document.getElementById('modal-body');

    const isEdit = !!data;

    switch (type) {
        case 'student':
            title.textContent = isEdit ? 'Edit Student' : 'Add Student';
            body.innerHTML = `
                <div class="field-group">
                    <label>Roll No</label>
                    <input type="text" id="field-rollNo" value="${isEdit ? escapeHtml(data.rollNo) : ''}" required ${isEdit ? 'disabled' : ''}>
                    ${isEdit ? '<small style="color:var(--text-muted)">Roll No cannot be changed after creation</small>' : ''}
                </div>
                <div class="field-group">
                    <label>Name</label>
                    <input type="text" id="field-name" value="${isEdit ? escapeHtml(data.name) : ''}" required>
                </div>
                <div class="field-group">
                    <label>Email</label>
                    <input type="email" id="field-email" value="${isEdit ? escapeHtml(data.email) : ''}" required>
                </div>
                <div class="field-group">
                    <label>Department</label>
                    <input type="text" id="field-department" value="${isEdit ? escapeHtml(data.department) : ''}" required>
                </div>
                <div class="field-group">
                    <label>Year</label>
                    <input type="number" id="field-year" value="${isEdit ? data.year : ''}" min="1" max="6" required oninput="handleStudentYearChange()">
                </div>
                <div class="field-group">
                    <label>Password ${isEdit ? '(Leave blank to keep current)' : ''}</label>
                    <input type="password" id="field-password" placeholder="${isEdit ? '••••••••' : 'Enter password'}" ${isEdit ? '' : 'required'}>
                </div>
                <div class="field-group">
                    <label>Subject Enrollment (Auto-filtered by Year)</label>
                    <div class="checkbox-group" id="enrollment-list" style="max-height: 150px; overflow-y: auto; background: rgba(255,255,255,0.05); padding: 10px; border-radius: 8px; margin-top: 8px;">
                        <!-- Will be populated by handleStudentYearChange -->
                    </div>
                </div>
            `;
            // Initial population
            setTimeout(() => {
                handleStudentYearChange(isEdit ? data.subjectIds : []);
            }, 0);
            break;

        case 'teacher':
            title.textContent = isEdit ? 'Edit Teacher' : 'Add Teacher';
            body.innerHTML = `
                <div class="field-group">
                    <label>Name</label>
                    <input type="text" id="field-name" value="${isEdit ? escapeHtml(data.name) : ''}" required>
                </div>
                <div class="field-group">
                    <label>Email</label>
                    <input type="email" id="field-email" value="${isEdit ? escapeHtml(data.email) : ''}" required>
                </div>
                <div class="field-group">
                    <label>Password ${isEdit ? '(Leave blank to keep current)' : ''}</label>
                    <input type="password" id="field-password" placeholder="${isEdit ? '••••••••' : 'Enter password'}" ${isEdit ? '' : 'required'}>
                </div>
            `;
            break;

        case 'subject':
            title.textContent = isEdit ? 'Edit Subject' : 'Add Subject';
            const teacherOptions = teachersCache.map(t =>
                `<option value="${t.id}" ${isEdit && data.assignedTeacherId === t.id ? 'selected' : ''}>${escapeHtml(t.name)}</option>`
            ).join('');
            body.innerHTML = `
                <div class="field-group">
                    <label>Subject Name</label>
                    <input type="text" id="field-subjectName" value="${isEdit ? escapeHtml(data.subjectName) : ''}" required>
                </div>
                <div class="field-group">
                    <label>Subject Code</label>
                    <input type="text" id="field-subjectCode" value="${isEdit ? escapeHtml(data.subjectCode) : ''}" required>
                </div>
                <div class="field-group">
                    <label>Assigned Teacher</label>
                    <select id="field-teacherId">
                        <option value="">-- Select Teacher --</option>
                        ${teacherOptions}
                    </select>
                </div>
                <div class="field-group">
                    <label>Applicable Year</label>
                    <input type="number" id="field-subjectYear" value="${isEdit ? data.year : ''}" min="1" max="4" required placeholder="e.g., 3 for 3rd year">
                </div>
            `;
            break;


        case 'exam':
            title.textContent = isEdit ? 'Edit Exam' : 'Schedule Exam';
            const subjectOptions = subjectsCache.map(s =>
                `<option value="${s.id}" ${isEdit && data.subjectId === s.id ? 'selected' : ''}>${escapeHtml(s.subjectName)} (${escapeHtml(s.subjectCode)})</option>`
            ).join('');
            body.innerHTML = `
                <div class="field-group">
                    <label>Exam Name</label>
                    <input type="text" id="field-examName" value="${isEdit ? escapeHtml(data.examName) : ''}" required placeholder="e.g., Cycle Test 1">
                </div>
                <div class="field-group">
                    <label>Exam Type</label>
                    <select id="field-examType" required>
                        <option value="" disabled ${!isEdit ? 'selected' : ''}>-- Select Type --</option>
                        <option value="CYCLE_TEST" ${isEdit && data.examType === 'CYCLE_TEST' ? 'selected' : ''}>🔄 Cycle Test</option>
                        <option value="INTERNAL" ${isEdit && data.examType === 'INTERNAL' ? 'selected' : ''}>📝 Internal</option>
                        <option value="SEMESTER" ${isEdit && data.examType === 'SEMESTER' ? 'selected' : ''}>🎓 Semester</option>
                    </select>
                </div>
                <div class="field-group">
                    <label>Subject</label>
                    <select id="field-examSubjectId" required>
                        <option value="">-- Select Subject --</option>
                        ${subjectOptions}
                    </select>
                </div>
                <div class="field-group">
                    <label>Target Year (e.g., 3 for 3rd year)</label>
                    <input type="number" id="field-targetYear" value="${isEdit ? data.targetYear : ''}" min="1" max="6" required>
                </div>
                <div class="field-group">
                    <label>Max Marks</label>
                    <input type="number" id="field-maxMarks" value="${isEdit ? data.maxMarks : ''}" min="1" required placeholder="e.g., 20">
                </div>
                <div class="field-group">
                    <label>Exam Date</label>
                    <input type="date" id="field-examDate" value="${isEdit && data.examDate ? data.examDate : ''}">
                </div>
                <div class="field-group">
                    <label>Description (optional)</label>
                    <input type="text" id="field-examDescription" value="${isEdit && data.description ? escapeHtml(data.description) : ''}" placeholder="Brief description">
                </div>
            `;
            break;
    }

    overlay.style.display = 'flex';
    setTimeout(() => {
        const firstInput = body.querySelector('input, select');
        if (firstInput) firstInput.focus();
    }, 100);
}

function closeModal(event) {
    if (event && event.target !== event.currentTarget) return;
    document.getElementById('modal-overlay').style.display = 'none';
    currentEditId = null;
    currentModalType = '';
}

async function handleFormSubmit(event) {
    event.preventDefault();

    try {
        switch (currentModalType) {
            case 'student': {
                const payload = {
                    rollNo: document.getElementById('field-rollNo').value.trim(),
                    name: document.getElementById('field-name').value.trim(),
                    email: document.getElementById('field-email').value.trim(),
                    department: document.getElementById('field-department').value.trim(),
                    year: parseInt(document.getElementById('field-year').value),
                    password: document.getElementById('field-password').value,
                    subjectIds: Array.from(document.querySelectorAll('input[name="subject-enroll"]:checked')).map(cb => parseInt(cb.value))
                };
                if (currentEditId) {
                    await apiCall(`/api/admin/students/${currentEditId}`, 'PUT', payload);
                    showToast('Student updated successfully', 'success');
                } else {
                    await apiCall('/api/admin/students', 'POST', payload);
                    showToast('Student created successfully', 'success');
                }
                closeModal();
                await loadStudents();
                break;
            }
            case 'teacher': {
                const payload = {
                    name: document.getElementById('field-name').value.trim(),
                    email: document.getElementById('field-email').value.trim(),
                    password: document.getElementById('field-password').value
                };
                if (currentEditId) {
                    await apiCall(`/api/admin/teachers/${currentEditId}`, 'PUT', payload);
                    showToast('Teacher updated successfully', 'success');
                } else {
                    await apiCall('/api/admin/teachers', 'POST', payload);
                    showToast('Teacher created successfully', 'success');
                }
                closeModal();
                await loadTeachers();
                break;
            }
            case 'subject': {
                const teacherId = document.getElementById('field-teacherId').value;
                const payload = {
                    subjectName: document.getElementById('field-subjectName').value.trim(),
                    subjectCode: document.getElementById('field-subjectCode').value.trim(),
                    assignedTeacherId: teacherId ? parseInt(teacherId) : null,
                    year: parseInt(document.getElementById('field-subjectYear').value)
                };
                if (currentEditId) {
                    await apiCall(`/api/admin/subjects/${currentEditId}`, 'PUT', payload);
                    showToast('Subject updated successfully', 'success');
                } else {
                    await apiCall('/api/admin/subjects', 'POST', payload);
                    showToast('Subject created successfully', 'success');
                }
                closeModal();
                await loadSubjects();
                break;
            }
            case 'exam': {
                const examPayload = {
                    examName: document.getElementById('field-examName').value.trim(),
                    examType: document.getElementById('field-examType').value,
                    subjectId: parseInt(document.getElementById('field-examSubjectId').value),
                    targetYear: parseInt(document.getElementById('field-targetYear').value),
                    maxMarks: parseInt(document.getElementById('field-maxMarks').value),
                    examDate: document.getElementById('field-examDate').value || null,
                    description: document.getElementById('field-examDescription').value.trim() || null
                };
                if (currentEditId) {
                    await apiCall(`/api/admin/exams/${currentEditId}`, 'PUT', examPayload);
                    showToast('Exam updated successfully', 'success');
                } else {
                    await apiCall('/api/admin/exams', 'POST', examPayload);
                    showToast('Exam scheduled successfully! 📋', 'success');
                }
                closeModal();
                await loadExams();
                break;
            }
            case 'exam-results-bulk': {
                const inputs = document.querySelectorAll('.exam-result-input');
                const results = [];
                inputs.forEach(input => {
                    if (input.value !== '') {
                        const marks = parseInt(input.value);
                        if (isNaN(marks)) return; // Skip invalid entries

                        results.push({
                            examId: parseInt(input.dataset.examId),
                            studentId: parseInt(input.dataset.studentId),
                            studentRollNo: input.dataset.studentRollNo,
                            marksObtained: marks
                        });
                    }
                });
                if (results.length === 0) {
                    showToast('Please enter at least one result', 'error');
                    return;
                }
                await apiCall('/api/teacher/exams/results/bulk', 'POST', results);
                showToast(`${results.length} result(s) saved successfully! ✅`, 'success');
                closeModal();
                await loadTeacherExams();
                break;
            }
        }
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}

// ==================== SEARCH ====================
function handleSearch(query) {
    query = query.toLowerCase().trim();

    switch (currentPage) {
        case 'students': {
            const filtered = studentsCache.filter(s =>
                (s.rollNo && s.rollNo.toLowerCase().includes(query)) ||
                (s.name && s.name.toLowerCase().includes(query)) ||
                (s.email && s.email.toLowerCase().includes(query)) ||
                (s.department && s.department.toLowerCase().includes(query))
            );
            renderStudents(filtered);
            break;
        }
        case 'teachers': {
            const filtered = teachersCache.filter(t =>
                (t.name && t.name.toLowerCase().includes(query)) ||
                (t.email && t.email.toLowerCase().includes(query))
            );
            renderTeachers(filtered);
            break;
        }
        case 'subjects': {
            const filtered = subjectsCache.filter(s =>
                (s.subjectName && s.subjectName.toLowerCase().includes(query)) ||
                (s.subjectCode && s.subjectCode.toLowerCase().includes(query))
            );
            renderSubjects(filtered);
            break;
        }
    }
}

// ==================== TOAST ====================
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    const icons = { success: '✅', error: '❌', info: 'ℹ️' };
    toast.innerHTML = `<span>${icons[type] || 'ℹ️'}</span> ${escapeHtml(message)}`;

    toast.onclick = () => toast.remove();
    container.appendChild(toast);

    setTimeout(() => {
        if (toast.parentNode) {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(50px)';
            toast.style.transition = '0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }
    }, 4000);
}

// Helper to refresh enrollment list based on year
function handleStudentYearChange(preSelectedIds = null) {
    const yearInput = document.getElementById('field-year');
    const list = document.getElementById('enrollment-list');
    if (!yearInput || !list) return;

    const selectedYear = parseInt(yearInput.value);

    // If we're not inside the 1st render, we should capture currently checked IDs
    // so they are not lost when changing years UNLESS they don't belong to the new year
    let currentChecked = preSelectedIds || Array.from(document.querySelectorAll('input[name="subject-enroll"]:checked'))
        .map(i => parseInt(i.value));

    const eligibleSubjects = subjectsCache.filter(s => s.year === selectedYear);

    if (eligibleSubjects.length === 0) {
        list.innerHTML = `<p style="font-size: 0.8rem; color: var(--text-muted);">No subjects found for Year ${selectedYear || '?'}.</p>`;
        return;
    }

    list.innerHTML = eligibleSubjects.map(s => `
        <div style="display: flex; align-items: center; margin-bottom: 8px;">
            <input type="checkbox" name="subject-enroll" value="${s.id}" id="sub-${s.id}" 
                   ${currentChecked.includes(s.id) ? 'checked' : ''} 
                   style="width: auto; margin-right: 10px;">
            <label for="sub-${s.id}" style="margin: 0; cursor: pointer; font-size: 0.9rem;">
                ${escapeHtml(s.subjectName)} (${escapeHtml(s.subjectCode)})
            </label>
        </div>
    `).join('');
}

// ==================== UTILITIES ====================
function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

// Keyboard shortcut: Escape to close modal
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeModal();
});
