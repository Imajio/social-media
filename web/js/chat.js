let checkBoxInputForMenu = document.getElementById("checkBoxInputForMenu");
let menu = document.querySelector(".menu");
let shkafMenu = document.querySelector("#shkafMenu");
let h3Menu = document.querySelector("#h3Menu");
let addcontactform = document.querySelector(".addcontact");
let nickname = document.querySelector("#nickname");
let i = 0;
let addchatbutton = document.querySelector("#addchatbutton");

nickname.addEventListener('input', function() {
    if (nickname.value != '' && nickname.value != getCookie("nickname")) {
        addchatbutton.removeAttribute('disabled');
        addchatbutton.style.cursor = "pointer";
    } else {
        addchatbutton.setAttribute('disabled', 'disabled');
        addchatbutton.style.cursor = "not-allowed";
    }
});

document.querySelector(".addcontactform").addEventListener("submit", function(event) {
    event.preventDefault();

    let nicknamevalue = nickname.value;

    if (nicknamevalue != null && nicknamevalue != "") {
        newchatafteraddpressed(getCookie("nickname"), nicknamevalue);
        console.log("Your nick from cookie: " + getCookie("nickname") + "\nNickname that you wanna add is: " + nicknamevalue);  
    } else {console.log("enter nickname");}
});

function deleteCookie(name) {
    document.cookie = name + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
}

function getCookie(name) {
    const cookies = document.cookie.split(';');
    for (let i = 0; i < cookies.length; i++) {
        const cookie = cookies[i].trim();
        if (cookie.startsWith(name + '=')) {
            return cookie.substring(name.length + 1);
        }
    }
    return null;
}

// window.addEventListener('load', function() {
//     if (getCookie("nickname") == null) {
//         window.location.href = "index.html";
//     } 
// });

checkBoxInputForMenu.addEventListener("click", function() {
    if (checkBoxInputForMenu.checked) {
        menu.style.left = "0";
        shkafMenu.style.opacity = "1";
        addcontactform.style.left = "310px";
    } else {
        menu.style.left = "-302px";
        shkafMenu.style.opacity = "0";
        addcontactform.style.left = "-310px";
        

        nickname.style.width = "0px";
        nickname.style.opacity = "0";
        addcontactform.style.opacity = "0";
        addcontactform.style.width = "0px";
        i--;

    }
});

function newchatonpress() {
    if (i === 0) {
        addcontactform.style.width = "310px";
        addcontactform.style.opacity = "1";
        nickname.style.opacity = "1";
        nickname.style.width = "240px";
        i++;
    } else {
        newchatafteraddpressed();
        nickname.style.width = "0px";
        nickname.style.opacity = "0";
        addcontactform.style.opacity = "0";
        addcontactform.style.width = "0px";
        i--;
    }
}


function newchatafteraddpressed(user1, user2) {
    let dataToSend = {
        user1: user1,
        user2: user2
    };

    fetch('http://localhost:8000/getDataForChat', {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dataToSend)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response not ok');
        }
        return response.json();
    })
    .then(data => {
        console.log(data);
        switch(data.message) {
            default: {
                console.log("Some error with chat creation!");
                break;
            }
            case("Chat could be created!"): {
                ifChatCouldBeCreatedCreateChat();
                break;
            }
            case("null"): {
                console.error("Some error on backend side");
                break;
            }
        }
    })
    .catch(error => {
        console.error('Error:', error.message);
    });
}

function ifChatCouldBeCreatedCreateChat() {
    console.log("Chat was created between: " + " You -> " + getCookie("nickname") + " | Other humanoid -> " + nickname.value);
    
}


function showNews(newsCount) {
    if (newsCount !== "none") {

    }
}

function redirectToProfile() {
    window.location.href = "profile.html";
}