const registerForm = document.getElementById("registerForm");

const nameField = document.getElementById("name");
const usernameField = document.getElementById("username");
const emailField = document.getElementById("email");
const passwordField = document.getElementById("password");
const confirmPasswordField = document.getElementById("confirmPassword");
const message = document.getElementById("registerMessage");


// ==========================================
// GENERATE USERNAME AUTOMATICALLY
// ==========================================

nameField.addEventListener("input", function () {

    const name = nameField.value.trim();

    const username = name
        .toLowerCase()
        .replace(/\s+/g, "")
        .replace(/[^a-z0-9]/g, "");

    usernameField.value = username;
});


// ==========================================
// REGISTRATION
// ==========================================

registerForm.addEventListener("submit", function (event) {

    event.preventDefault();

    const name = nameField.value.trim();
    const username = usernameField.value.trim();
    const email = emailField.value.trim();
    const password = passwordField.value;
    const confirmPassword = confirmPasswordField.value;


    // Check required fields
    if (
        name === "" ||
        username === "" ||
        email === "" ||
        password === "" ||
        confirmPassword === ""
    ) {

        message.textContent =
            "Please fill in all required fields.";

        message.className = "error-message";

        return;
    }


    // Check password match
    if (password !== confirmPassword) {

        message.textContent =
            "Passwords do not match.";

        message.className = "error-message";

        return;
    }


    // Send registration data to servlet
    fetch("auth", {

        method: "POST",

        headers: {
            "Content-Type":
                "application/x-www-form-urlencoded"
        },

        body:
            "action=register" +
            "&username=" + encodeURIComponent(username) +
            "&name=" + encodeURIComponent(name) +
            "&email=" + encodeURIComponent(email) +
            "&password=" + encodeURIComponent(password)

    })

    .then(response => response.json())

    .then(data => {

        if (data.success) {

            message.innerHTML =
                "Registration successful!<br>" +
                "Your username is: <strong>" +
                username +
                "</strong>";

            message.className = "success-message";


            // Temporary frontend storage
            localStorage.setItem(
                "foodShareUser",
                JSON.stringify({
                    username: username,
                    name: name,
                    email: email,
                    password: password
                })
            );


            // Go to login page
            setTimeout(function () {

                window.location.href =
                    "login.html";

            }, 2500);

        }

        else {

            message.textContent =
                data.message ||
                "Registration failed.";

            message.className =
                "error-message";
        }

    })

    .catch(error => {

        console.error(error);

        message.textContent =
            "Unable to connect to the server.";

        message.className =
            "error-message";
    });

});