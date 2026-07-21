document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('#studentSignupForm');
    if (!form) return;

    const invitationCode = document.querySelector('#invitationCode');
    const courseSelect = document.querySelector('#courseId');
    const studentSelect = document.querySelector('#studentId');
    const loginId = document.querySelector('#loginId');
    const loginIdCheckButton = document.querySelector('#loginIdCheckButton');
    const password = document.querySelector('#password');
    const passwordConfirm = document.querySelector('#passwordConfirm');
    const invitationStatus = document.querySelector('#invitationStatus');
    const studentStatus = document.querySelector('#studentStatus');
    const loginIdStatus = document.querySelector('#loginIdStatus');
    const passwordStatus = document.querySelector('#passwordStatus');

    const initialCourseId = form.dataset.selectedCourseId || '';
    const initialStudentId = form.dataset.selectedStudentId || '';
    let checkedLoginId = '';
    let invitationTimer;
    let invitationRequest = 0;

    const setStatus = (element, message, state = '') => {
        element.textContent = message;
        element.classList.toggle('success', state === 'success');
        element.classList.toggle('error', state === 'error');
    };

    const readError = async (response, fallback) => {
        try {
            const body = await response.json();
            return body.message || fallback;
        } catch (_) {
            return fallback;
        }
    };

    const resetStudents = (message = '반을 먼저 선택해 주세요') => {
        studentSelect.innerHTML = `<option value="">${message}</option>`;
        studentSelect.disabled = true;
        setStatus(studentStatus, '');
    };

    const loadStudents = async (selectedStudentId = '') => {
        resetStudents('학생 목록을 불러오는 중입니다...');
        const courseId = courseSelect.value;
        if (!courseId) {
            resetStudents();
            return;
        }

        const query = new URLSearchParams({ invitationCode: invitationCode.value.trim(), courseId });
        const response = await fetch(`/api/auth/student-signup/students?${query}`);
        if (!response.ok) {
            resetStudents('학생 목록을 불러올 수 없습니다');
            setStatus(studentStatus, await readError(response, '학생 목록을 불러오지 못했습니다.'), 'error');
            return;
        }

        const students = await response.json();
        studentSelect.innerHTML = '<option value="">학생 이름을 선택해 주세요</option>';
        students.forEach(student => {
            const option = new Option(student.name, student.id);
            option.selected = String(student.id) === String(selectedStudentId);
            studentSelect.add(option);
        });
        studentSelect.disabled = students.length === 0;
        setStatus(
            studentStatus,
            students.length === 0 ? '가입 가능한 학생이 없습니다. 선생님께 학생 등록 여부를 확인해 주세요.' : `${students.length}명의 학생을 선택할 수 있습니다.`,
            students.length === 0 ? 'error' : 'success'
        );
    };

    const loadCourses = async (selectedCourseId = '', selectedStudentId = '') => {
        const code = invitationCode.value.trim();
        const requestId = ++invitationRequest;
        courseSelect.innerHTML = '<option value="">반 목록을 불러오는 중입니다...</option>';
        courseSelect.disabled = true;
        resetStudents();

        if (code.length < 24) {
            courseSelect.innerHTML = '<option value="">초대코드를 먼저 입력해 주세요</option>';
            setStatus(invitationStatus, '선생님께 받은 24자리 초대코드를 입력해 주세요.');
            return;
        }

        const response = await fetch(`/api/auth/student-signup/courses?${new URLSearchParams({ invitationCode: code })}`);
        if (requestId !== invitationRequest) return;
        if (!response.ok) {
            courseSelect.innerHTML = '<option value="">반 목록을 불러올 수 없습니다</option>';
            setStatus(invitationStatus, await readError(response, '초대코드를 확인해 주세요.'), 'error');
            return;
        }

        const courses = await response.json();
        courseSelect.innerHTML = '<option value="">반을 선택해 주세요</option>';
        courses.forEach(course => {
            const option = new Option(course.name, course.id);
            option.selected = String(course.id) === String(selectedCourseId);
            courseSelect.add(option);
        });
        courseSelect.disabled = courses.length === 0;
        setStatus(
            invitationStatus,
            courses.length === 0 ? '등록된 반이 없습니다. 선생님께 문의해 주세요.' : `${courses.length}개의 반을 불러왔습니다.`,
            courses.length === 0 ? 'error' : 'success'
        );
        if (courseSelect.value) await loadStudents(selectedStudentId);
    };

    invitationCode.addEventListener('input', () => {
        invitationCode.value = invitationCode.value.toUpperCase();
        clearTimeout(invitationTimer);
        invitationTimer = setTimeout(() => loadCourses(), 450);
    });
    courseSelect.addEventListener('change', () => loadStudents());

    loginId.addEventListener('input', () => {
        loginId.value = loginId.value.toLowerCase();
        checkedLoginId = '';
        setStatus(loginIdStatus, '아이디 중복 확인이 필요합니다.');
    });

    loginIdCheckButton.addEventListener('click', async () => {
        if (!loginId.checkValidity()) {
            loginId.reportValidity();
            return;
        }
        const value = loginId.value.trim().toLowerCase();
        const response = await fetch(`/api/auth/student-signup/login-id-availability?${new URLSearchParams({ loginId: value })}`);
        if (!response.ok) {
            checkedLoginId = '';
            setStatus(loginIdStatus, await readError(response, '아이디를 확인해 주세요.'), 'error');
            return;
        }
        const result = await response.json();
        checkedLoginId = result.available ? value : '';
        setStatus(loginIdStatus, result.message, result.available ? 'success' : 'error');
    });

    const validatePasswords = () => {
        if (!passwordConfirm.value) {
            setStatus(passwordStatus, '');
            return false;
        }
        const matches = password.value === passwordConfirm.value;
        setStatus(passwordStatus, matches ? '비밀번호가 일치합니다.' : '비밀번호가 일치하지 않습니다.', matches ? 'success' : 'error');
        return matches;
    };
    password.addEventListener('input', validatePasswords);
    passwordConfirm.addEventListener('input', validatePasswords);

    form.addEventListener('submit', event => {
        const normalizedLoginId = loginId.value.trim().toLowerCase();
        if (checkedLoginId !== normalizedLoginId) {
            event.preventDefault();
            setStatus(loginIdStatus, '회원가입 전에 아이디 중복 확인을 해주세요.', 'error');
            loginId.focus();
            return;
        }
        if (!validatePasswords()) {
            event.preventDefault();
            passwordConfirm.focus();
        }
    });

    if (invitationCode.value.trim().length === 24) {
        loadCourses(initialCourseId, initialStudentId);
    }
});
