let checkBoxInputForMenu = document.getElementById("checkBoxInputForMenu");
let menu = document.querySelector(".menu");
let shkafMenu = document.querySelector("#shkafMenu");
let h3Menu = document.querySelector("#h3Menu");
let addcontactform = document.querySelector(".addcontact");
let nickname = document.querySelector("#nickname");
let i = 0;
let addchatbutton = document.querySelector("#addchatbutton");

nickname.addEventListener('input', function () {
    if (nickname.value != '' && nickname.value != getCookie("nickname")) {
        addchatbutton.removeAttribute('disabled');
        addchatbutton.style.cursor = "pointer";
    } else {
        addchatbutton.setAttribute('disabled', 'disabled');
        addchatbutton.style.cursor = "not-allowed";
    }
});

document.querySelector(".addcontactform").addEventListener("submit", function (event) {
    event.preventDefault();

    console.log(nickname.value);

    if (nickname.value != null && nickname.value != "") {
        newchatafteraddpressed(getCookie("nickname"), nickname.value);
        console.log("Your nick from cookie: " + getCookie("nickname") + "\nNickname that you wanna add is: " + nickname.value);
    } else { console.log("enter nickname"); }
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

window.addEventListener('load', function () {
    if (getCookie("nickname") == null) {
        window.location.href = "index.html";
    }
});

checkBoxInputForMenu.addEventListener("click", function () {
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
        // newchatafteraddpressed();
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
            switch (data.message) {
                default: {
                    console.log("Some error with chat creation!");
                    break;
                }
                case ("Chat could be created!"): {
                    ifChatCouldBeCreatedCreateChat(nickname.value, "Hello, how is it going?");
                    menu.style.left = "-302px";
                    shkafMenu.style.opacity = "0";
                    addcontactform.style.left = "-310px";


                    nickname.style.width = "0px";
                    nickname.style.opacity = "0";
                    addcontactform.style.opacity = "0";
                    addcontactform.style.width = "0px";
                    i--;


                    checkBoxInputForMenu.checked = false;
                    break;
                }
                case ("null"): {
                    console.error("Some error on backend side");
                    break;
                }
            }
        })
        .catch(error => {
            console.error('Error:', error.message);
        });
}

function ifChatCouldBeCreatedCreateChat(nick, last) {
    console.log("Chat was created between: " + " You -> " + getCookie("nickname") + " | Other humanoid -> " + nickname.value);
    addChatToChatsPeople("../media/depositphotos_54081723-stock-photo-beautiful-nature-landscape.jpg", nick, last);
}

function addChatToChatsPeople(avatarSrc, nicknameOfChat, lastMessage) {
    // Create new chat element
    let newChat = document.createElement('div');
    newChat.classList.add('chatsPeopleIn');
    newChat.setAttribute('onclick', 'openChatFunction(\"' + nicknameOfChat + '\")');

    // Create chat elements
    let chatAvatar = document.createElement('div');
    chatAvatar.classList.add('chatavatar');
    chatAvatar.innerHTML = `<img src="${avatarSrc}" alt="">`; // Avatar source

    let chatNickAndLast = document.createElement('div');
    chatNickAndLast.classList.add('chatnickandlast');

    let chatNickname = document.createElement('div');
    chatNickname.classList.add('chatnickname');
    chatNickname.textContent = nicknameOfChat; // Set nickname

    let chatLastMessage = document.createElement('div');
    chatLastMessage.classList.add('chatlastMessage');
    chatLastMessage.textContent = lastMessage; // Set last message

    // Add chat element to new chat
    chatNickAndLast.appendChild(chatNickname);
    chatNickAndLast.appendChild(chatLastMessage);

    newChat.appendChild(chatAvatar);
    newChat.appendChild(chatNickAndLast);

    // Add new chat to block .chatsPeople
    let chatsPeople = document.querySelector('.chatsPeople');
    chatsPeople.appendChild(newChat);
}


function showNews(newsCount) {
    if (newsCount !== "none") {

    }
}

function redirectToProfile() {
    window.location.href = "profile.html";
}

// window.addEventListener('load', takeAllChatsOfUserFromDataBase());

// function takeAllChatsOfUserFromDataBase() {
fetch('http://localhost:8000/takeAllChatsOfUserFromDataBase', {
    method: 'POST',
    body: getCookie("nickname")
})
    .then(response => response.json())
    .then(data => {
        for (let key in data) {
            if (data.hasOwnProperty(key)) {
                let abc = data[key];
                addChatToChatsPeople("../media/depositphotos_54081723-stock-photo-beautiful-nature-landscape.jpg", abc, "Say Hello!");
            }
        }
    })
    .catch(error => {
        console.log("No chats yet");
    });
//}

function openChatFunction(nickOnHeader) {
    let ifChatAllreadyOpened = document.querySelector('.chatPlaceWithMessages');

    if (!ifChatAllreadyOpened) {
        let lastActivity = "last activity";
        const hero = document.querySelector('.hero');

        const messagesArea = document.createElement('div');
        messagesArea.classList.add('chatPlaceWithMessages');


        const header = document.createElement('div');
        header.classList.add('chatPlaceWithMessagesHeader');

        const headerData = document.createElement('div');
        headerData.classList.add('chatPlaceWithMessagesHeaderPData');
        headerData.innerHTML = '<img src=\'../media/depositphotos_54081723-stock-photo-beautiful-nature-landscape.jpg\' class=\'chatPlaceWithMessagesHeaderAvatar\'>'
        + '<p class=\'chatPlaceWithMessagesHeaderParagraf\'>' + 
        nickOnHeader + '</p>\n' + '<p class=\'chatPlaceWithMessagesHeaderLastActivity\'>' + lastActivity + '</p>';

        header.appendChild(headerData);

        const messagesAreaWithoutFooterAndHeader = document.createElement('div');
        messagesAreaWithoutFooterAndHeader.classList.add('messages-area');

        const footer = document.createElement('div');
        footer.classList.add('chatPlaceWithMessagesFooter');

        const placeWithInputs = document.createElement('div');
        placeWithInputs.classList.add('textInputWithMessage');
        placeWithInputs.innerHTML = '<input type="text" name="" id="inputForMessageOnFooterOfChat" placeholder="Message" autocomplete="off">';

        const cRipple = document.createElement('div');
        cRipple.classList.add('cRipple');
        cRipple.innerHTML = '<img src="../media/paper-plane.png" alt="" onclick=\"onPressSendMessageButton(\'' + nickOnHeader + '\')\">';

        placeWithInputs.appendChild(cRipple);
        footer.appendChild(placeWithInputs);
        messagesArea.appendChild(header);
        messagesArea.appendChild(messagesAreaWithoutFooterAndHeader);
        messagesArea.appendChild(footer);

        hero.appendChild(messagesArea);
        document.querySelector('.background').style.filter = 'brightness(0.55)';
    } else {
        let lastActivity = "last activity";
        const headerData = document.querySelector('.chatPlaceWithMessagesHeaderPData');
        headerData.innerHTML = '<img src=\'../media/depositphotos_54081723-stock-photo-beautiful-nature-landscape.jpg\' class=\'chatPlaceWithMessagesHeaderAvatar\'>'
        + '<p class=\'chatPlaceWithMessagesHeaderParagraf\'>' + 
        nickOnHeader + '</p>\n' + '<p class=\'chatPlaceWithMessagesHeaderLastActivity\'>' + lastActivity + '</p>';

        const inputForMessage = document.querySelector('.textInputWithMessage');
        inputForMessage.innerHTML = '<input type="text" name="" id="inputForMessageOnFooterOfChat" placeholder="Message" autocomplete="off">';
        
        const cRipple = document.createElement('div');
        cRipple.classList.add('cRipple');
        cRipple.innerHTML = '<img src="../media/paper-plane.png" alt="" onclick=\"onPressSendMessageButton(\'' + nickOnHeader + '\')\">';

        inputForMessage.appendChild(cRipple);
    }

}

function onPressSendMessageButton(nickofreceiver) {
    let message = document.querySelector('#inputForMessageOnFooterOfChat').value;

    if (message && message != "" && message != null) {

        const ownMessageArea = document.createElement('div');
        ownMessageArea.classList.add('own-message-area');

        const ownMessage = document.createElement('div');
        ownMessage.classList.add('own-message');
        ownMessage.innerText = message;

        const messagesArea = document.querySelector('.messages-area');

        ownMessageArea.appendChild(ownMessage);
        messagesArea.appendChild(ownMessageArea);


        sendMessageToDataBase(getCookie("nickname"), nickofreceiver, message);

        //clearing message input
        document.querySelector('#inputForMessageOnFooterOfChat').value = null;
    }
}

function sendMessageToDataBase(sender_nick,receiver_nick,message) {

    console.log(sender_nick + "->" + receiver_nick + "\nMessage: " + message);

    fetch('http://localhost:8000/sendMessage', {
        method: 'POST',
        body: sender_nick + ","  + receiver_nick + ","  + message
    })
        .then(response => response.json())
        .then(data => {
            

        })
        .catch(error => {
            console.error(error.message);
        });

}