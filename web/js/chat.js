function addNewChat(user) {
  
}

let checkBoxInputForMenu = document.getElementById("checkBoxInputForMenu");
let menu = document.querySelector(".menu");
let shkafMenu = document.querySelector("#shkafMenu");
let h3Menu = document.querySelector("#h3Menu");

checkBoxInputForMenu.addEventListener("click", function() {
    if (checkBoxInputForMenu.checked) {
        menu.style.left = "0";
        shkafMenu.style.opacity = "1";
    } else {
        menu.style.left = "-302px";
        shkafMenu.style.opacity = "0";
    }
});





function showNews(newsCount) {
    if (newsCount !== "none") {

    }
}

function redirectToProfile() {
    window.location.href = "profile.html";
}