document.addEventListener('DOMContentLoaded', () => {
    const courseSelect = document.querySelector('#progressCourse');
    const scheduleSelect = document.querySelector('#progressSchedule');
    const scheduleHelp = document.querySelector('#scheduleHelp');
    const memo = document.querySelector('#progressMemo');
    const memoLength = document.querySelector('#memoLength');

    if (courseSelect && scheduleSelect) {
        const filterSchedules = (keepSelection) => {
            const courseId = courseSelect.value;
            let matchingCount = 0;

            Array.from(scheduleSelect.options).forEach((option, index) => {
                if (index === 0) return;
                const matches = courseId !== '' && option.dataset.courseId === courseId;
                option.hidden = !matches;
                option.disabled = !matches;
                if (matches) matchingCount += 1;
            });

            if (!keepSelection || scheduleSelect.selectedOptions[0]?.disabled) {
                scheduleSelect.value = '';
            }
            scheduleSelect.disabled = courseId === '' || matchingCount === 0;
            if (scheduleHelp) {
                scheduleHelp.textContent = courseId === ''
                    ? '반을 선택하면 해당 반의 시간표가 표시됩니다.'
                    : matchingCount === 0
                        ? '선택한 반에 등록된 시간표가 없습니다.'
                        : `${matchingCount}개의 시간표 중 선택할 수 있습니다.`;
            }
        };

        filterSchedules(true);
        courseSelect.addEventListener('change', () => filterSchedules(false));
    }

    if (memo && memoLength) {
        const updateLength = () => {
            memoLength.textContent = memo.value.length.toLocaleString('ko-KR');
        };
        updateLength();
        memo.addEventListener('input', updateLength);
    }
});
