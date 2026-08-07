document.addEventListener('DOMContentLoaded', function () {
    const idInput = document.getElementById('idNumber');

    if (idInput) {
        idInput.addEventListener('input', function (e) {
            // 1. Tanggalin ang lahat ng non-numeric characters
            let value = e.target.value.replace(/\D/g, '');

            // 2. Limit sa maximum 12 digits (00-0000-000000)
            if (value.length > 12) {
                value = value.slice(0, 12);
            }

            // 3. Auto-insert ng hyphens
            let formattedValue = '';
            if (value.length > 0) {
                formattedValue += value.substring(0, 2);
            }
            if (value.length > 2) {
                formattedValue += '-' + value.substring(2, 6);
            }
            if (value.length > 6) {
                formattedValue += '-' + value.substring(6, 12);
            }

            // 4. Update ang input field value
            e.target.value = formattedValue;
        });
    }
});