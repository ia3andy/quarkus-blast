import {Howl, Howler} from 'howler';

const sounds = {
    blasted: new Howl({src: ["/sounds/blasted.wav"]}),
    up: new Howl({src: ["/sounds/up.wav"]}),
    down: new Howl({src: ["/sounds/down.wav"]}),
    charm: new Howl({src: ["/sounds/charm.wav"]}),
    strange: new Howl({src: ["/sounds/strange.wav"]}),
    top: new Howl({src: ["/sounds/top.wav"]}),
    bottom: new Howl({src: ["/sounds/bottom.wav"]})
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
