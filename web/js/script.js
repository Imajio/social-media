
//HTTP data transfer
document.getElementById('myForm').addEventListener('submit', function (event) {
  event.preventDefault();

  let formData = new FormData(this);
  let formLogin = document.querySelector("#login").value;
  let formPassword = document.querySelector("#password").value;

  if (formLogin !== " " || formPassword !== " ") {
    let hashedPassword = hashPassword(formPassword);
    formData.append('login', formLogin);
    formData.append('password', hashedPassword);

    fetch('http://localhost:8000/receiveData', {
      method: 'POST',
      body: formData
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Error HTTP: ' + response.status);
        }
        return response.text(); // Возвращаем ответ сервера
      })
      .then(data => {




        switch (data) {
          default: {
            console.log("Error with redirect or received data");
            break;
          }
          case ("Success: Data received and matched!"): {
            window.location.href = "chat.html";
            break;
          }
          case ("Data not exist in database!"): {
            console.log("Data not exist in database! \n You was registrated!");
            alert("Data not exist in database! \n You was registrated!");
            window.location.href = "chat.html";
            break;
          }
        }




      })
      .catch(error => {
        console.error('Error:', error);
      });
  } else {
    alert("Login and Password can be a space");
  }
});
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// Other logic /////////////////////////////////////////////////////////////////////////////////////////////////////////

// Hash pswd with SHA-256

function hashPassword(password) {
  let hashedPassword = CryptoJS.SHA256(password).toString();
  return hashedPassword;
}














// Beautiful Effects, and all/////////////////////////////////////////////////////////////////////////////////////////////
gsap.registerPlugin(ScrambleTextPlugin, MorphSVGPlugin);

const BLINK_SPEED = 0.075;
const TOGGLE_SPEED = 0.125;
const ENCRYPT_SPEED = 1;

let busy = false;

const EYE = document.querySelector('.eye');
const TOGGLE = document.querySelector('button');
const INPUT = document.querySelector('#password');
const PROXY = document.createElement('div');

const chars =
  'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789`~,.<>?/;":][}{+_)(*&^%$#@!±=-§';

let blinkTl;
const BLINK = () => {
  const delay = gsap.utils.random(2, 8);
  const duration = BLINK_SPEED;
  const repeat = Math.random() > 0.5 ? 3 : 1;
  blinkTl = gsap.timeline({
    delay,
    onComplete: () => BLINK(),
    repeat,
    yoyo: true
  }).

    to('.lid--upper', {
      morphSVG: '.lid--lower',
      duration
    }).

    to('#eye-open path', {
      morphSVG: '#eye-closed path',
      duration
    },
      0);
};

BLINK();

const posMapper = gsap.utils.mapRange(-100, 100, 30, -30);
let reset;
const MOVE_EYE = ({ x, y }) => {
  if (reset) reset.kill();
  reset = gsap.delayedCall(2, () => {
    gsap.to('.eye', { xPercent: 0, yPercent: 0, duration: 0.2 });
  });
  const BOUNDS = EYE.getBoundingClientRect();
  gsap.set('.eye', {
    xPercent: gsap.utils.clamp(-30, 30, posMapper(BOUNDS.x - x)),
    yPercent: gsap.utils.clamp(-30, 30, posMapper(BOUNDS.y - y))
  });

};

window.addEventListener('pointermove', MOVE_EYE);


TOGGLE.addEventListener('click', () => {
  if (busy) return;
  const isText = INPUT.matches('[type=password]');
  const val = INPUT.value;
  busy = true;
  TOGGLE.setAttribute('aria-pressed', isText);
  const duration = TOGGLE_SPEED;

  if (isText) {
    if (blinkTl) blinkTl.kill();

    gsap.timeline({
      onComplete: () => {
        busy = false;
      }
    })

      .to('.lid--upper', {
        morphSVG: '.lid--lower',
        duration
      }).

      to('#eye-open path', {
        morphSVG: '#eye-closed path',
        duration
      },
        0)
      .to(PROXY, {
        duration: ENCRYPT_SPEED,
        onStart: () => {
          INPUT.type = 'text';
        },
        onComplete: () => {
          PROXY.innerHTML = '';
          INPUT.value = val;
        },
        scrambleText: {
          chars,
          text:
            INPUT.value.charAt(INPUT.value.length - 1) === ' ' ?
              `${INPUT.value.slice(0, INPUT.value.length - 1)}${chars.charAt(
                Math.floor(Math.random() * chars.length))
              }` :
              INPUT.value
        },

        onUpdate: () => {
          const len = val.length - PROXY.innerText.length;
          INPUT.value = `${PROXY.innerText}${new Array(len).fill('•').join('')}`;
        }
      },
        0);
  } else {
    gsap.timeline({
      onComplete: () => {
        BLINK();
        busy = false;
      }
    }).

      to('.lid--upper', {
        morphSVG: '.lid--upper',
        duration
      }).

      to('#eye-open path', {
        morphSVG: '#eye-open path',
        duration
      },
        0).
      to(PROXY, {
        duration: ENCRYPT_SPEED,
        onComplete: () => {
          INPUT.type = 'password';
          INPUT.value = val;
          PROXY.innerHTML = '';
        },
        scrambleText: {
          chars,
          text: new Array(INPUT.value.length).fill('•').join('')
        },

        onUpdate: () => {
          INPUT.value = `${PROXY.innerText}${val.slice(
            PROXY.innerText.length,
            val.length)
            }`;
        }
      },
        0);
  }
});


const FORM = document.querySelector('form');
FORM.addEventListener('submit', event => event.preventDefault());

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////