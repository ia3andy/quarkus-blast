import "htmx.org";
import "idiomorph";
import _hyperscript from "hyperscript.org";
import "bootstrap";

_hyperscript.browserInit();

function random(min, max) {
    return Math.random() * (max - min) + min;
}
let blasting = false;
let timeout = null;
window.blast = function (element, name, volume) {
    const delay = blasting ? random(100, 300) : 0;
    blasting = true;
    clearTimeout(timeout);
    timeout = setTimeout(() => {
        blasting = false;
    });
    setTimeout(() => {
        element.classList.add("blast-effect");
        playSound(name, volume);
    }, delay);
}