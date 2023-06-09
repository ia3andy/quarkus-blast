import {Howl, Howler} from 'howler';

const sounds = {
    blasted: new Howl({src: ["/static/sounds/blasted.wav"]}),
    up: new Howl({src: ["/static/sounds/up.wav"]}),
    down: new Howl({src: ["/static/sounds/down.wav"]}),
    charm: new Howl({src: ["/static/sounds/charm.wav"]}),
    strange: new Howl({src: ["/static/sounds/strange.wav"]}),
    top: new Howl({src: ["/static/sounds/top.wav"]}),
    bottom: new Howl({src: ["/static/sounds/bottom.wav"]})
};

function random(min, max) {
    return Math.random() * (max - min) + min;
}

let playing = false;
let timeout = null;
window.playSound = function (name, volume) {
    const delay = playing ? random(300, 800) : 0;
    playing = true;
    clearTimeout(timeout);
    timeout = setTimeout(() => {
        playing = false;
    });
    console.log(delay);
    setTimeout(() => {
        const played = sounds[name].play();
        Howler.volume(volume, played);
    }, delay);
};
