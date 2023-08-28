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

window.playSound = function (name, volume) {
    const played = sounds[name].play();
    Howler.volume(volume, played);
};
