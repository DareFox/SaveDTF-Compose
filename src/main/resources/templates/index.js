const modal = document.querySelector(".modal");
const counter = document.querySelector(".gl-counter");
const currentimage = document.querySelector(".gl-current-image");
const galleries = document.querySelectorAll(".gall div");
const arrowleft = document.querySelector(".modal-arrow-left");
const arrowright = document.querySelector(".modal-arrow-right");
const closebtn = document.querySelector(".modal-exit-territory");
const images = document.querySelectorAll(".andropov_image");
const glfooter = document.querySelector(".gl-footer");
let imgsrc = [];
let glcount = 0;
let glnum = 0;
let modalopen = false;
let galleryopen = false;
galleries.forEach((gallery) => {
    gallery.addEventListener("click", (e) => {
        modal.classList.add("modal--open");
        modalopen = true;
        galleryopen = true;
        glcount = e.target.parentNode.childElementCount;
        for (var i = 0; i < glcount; i++) {
          imgsrc.push(e.target.parentNode.children[i].style.backgroundImage.slice(4, -1).replace(/"/g, ""));
        }
        glnum = e.target.getAttribute("pos");
        currentimage.src = imgsrc[glnum];
        counter.innerHTML = (Number(glnum) + 1) + " из " + glcount;
    });
});
images.forEach((image) => {
    image.addEventListener("click", (e) => {
      console.log(e.target.src);
      modal.classList.add("modal--open");
      closebtn.classList.add("exit-territory-image");
      modalopen = true;
      galleryopen = false;
      imgsrc.push(e.target.src);
      glcount = 1;
      glnum = 0;
      currentimage.src = imgsrc[glnum];
      glfooter.style.display = "none";
      arrowleft.style.display = "none";
      arrowright.style.display = "none";
    });
});
arrowleft.addEventListener("click", (e) => {
  glnum--;
  if(glnum < 0){
    glnum = glcount - 1;
  }
  currentimage.src = imgsrc[glnum];
  counter.innerHTML = Number(glnum + 1) + " из " + glcount;
});
arrowright.addEventListener("click", (e) => {
  glnum++;
  if(glnum > (glcount - 1)){
    glnum = 0;
  }
  currentimage.src = imgsrc[glnum];
  counter.innerHTML = Number(glnum + 1) + " из " + glcount;
});
closebtn.addEventListener("click", (e) => {
  modal.classList.remove("modal--open");
  currentimage.src = "";
  glrealnum = 0;
  glnum = 0;
  glcount = 0;
  imgsrc = [];
  modalopen = false;
  if(galleryopen){
    galleryopen = false;
    counter.innerHTML = "";
  } else {
    closebtn.classList.remove("exit-territory-image");
    glfooter.style.display = "block";
    arrowleft.style.display = "block";
    arrowright.style.display = "block";
  }
});
document.onkeydown = checkKey;
function checkKey(e) {
  e = e || window.event;
  if (modalopen == true){
    if (e.keyCode == '27') {
      modal.classList.remove("modal--open");
      currentimage.src = "";
      glrealnum = 0;
      glnum = 0;
      glcount = 0;
      imgsrc = [];
      modalopen = false;
      if(galleryopen){
        galleryopen = false;
        counter.innerHTML = "";
      } else {
        closebtn.classList.remove("exit-territory-image");
        glfooter.style.display = "block";
        arrowleft.style.display = "block";
        arrowright.style.display = "block";
      }
    } 
    if(galleryopen){
      if (e.keyCode == '37') {
        glnum--;
        if(glnum < 0){
          glnum = glcount - 1;
        }
        currentimage.src = imgsrc[glnum];
        counter.innerHTML = Number(glnum + 1) + " из " + glcount;
        modalopen = false;
        galleryopen = false;
      } else if (e.keyCode == '39') {
        glnum++;
        if(glnum > (glcount - 1)){
          glnum = 0;
        }
        currentimage.src = imgsrc[glnum];
        counter.innerHTML = Number(glnum + 1) + " из " + glcount;
        modalopen = false;
        galleryopen = false;
      }
    }
  }
}