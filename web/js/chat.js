function addNewChat(user) {
  
}

let checkBoxInputForMenu = document.getElementById("checkBoxInputForMenu");
let menu = document.querySelector(".menu");
let shkafMenu = document.querySelector("#shkafMenu");

checkBoxInputForMenu.addEventListener("click", function() {
    if (checkBoxInputForMenu.checked) {
        menu.style.left = "0";
        shkafMenu.style.opacity = "1";
    } else {
        menu.style.left = "-302px";
        shkafMenu.style.opacity = "0";
    }
});



document.addEventListener('click', function() {
    
});