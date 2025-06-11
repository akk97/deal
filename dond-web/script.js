let shuffledNumbers = [];
let shuffledAmounts = [];
let personalCaseNumber = null;
let personalCaseValue = null;
let clickCount = 0;
let balance = 0;
let valuesLeft = [];

const gameBoard = document.getElementById("gameBoard");
const balanceDisplay = document.getElementById("balance");
const resetBtn = document.getElementById("resetBtn");

let gameLog = {
  personalCaseNumber: null,
  personalCaseValue: null,
  openedCases: [],
  offers: [],
  acceptedOffer: null,
  finalResult: null,
};

function saveGameLog(log) {
const existingGameLog = JSON.parse(localStorage.getItem('fullGameLog')) || [];
existingGameLog.push(log);
localStorage.setItem('fullGameLog', JSON.stringify(existingGameLog))
console.log("gamelog:")
}


// Show a modal with message and buttons, returns a Promise resolved with button text clicked
function showModal(title, message, buttons) {
  return new Promise((resolve) => {
    const modal = document.getElementById("modal");
    const modalTitle = document.getElementById("modalTitle");
    const modalMessage = document.getElementById("modalMessage");
    const modalButtons = document.getElementById("modalButtons");

    modalTitle.textContent = title;
    modalMessage.textContent = message;

    // Clear previous buttons
    modalButtons.innerHTML = "";

    buttons.forEach(text => {
      const btn = document.createElement("button");
      btn.textContent = text;
      btn.onclick = () => {
        modal.style.display = "none";
        resolve(text);
      };
      modalButtons.appendChild(btn);
    });

    modal.style.display = "flex";
  });
}

// Shuffle helper function
function shuffle(arr) {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
}

// Initialize the game
function initGame() {

gameLog = {
  personalCaseNumber: null,
  personalCaseValue: null,
  openedCases: [],
  offers: [],
  acceptedOffer: null,
  finalResult: null,
};

  gameBoard.innerHTML = "";
  balance = 0;
  clickCount = 0;
  personalCaseNumber = null;
  personalCaseValue = null;

  shuffledNumbers = Array.from({ length: 16 }, (_, i) => i + 1);
  shuffledAmounts = [1, 5, 10, 25, 50, 75, 100, 200, 500, 700, 1000, 5000, 10000, 50000, 100000, 1000000];

  shuffle(shuffledNumbers);
  shuffle(shuffledAmounts);

  valuesLeft = [...shuffledAmounts];

  updateBalanceDisplay();

  // Create buttons
  for (let i = 0; i < 16; i++) {
    const btn = document.createElement("button");
    btn.textContent = shuffledNumbers[i];
    btn.dataset.number = shuffledNumbers[i];
    btn.addEventListener("click", () => handleClick(btn));
    gameBoard.appendChild(btn);
  }


}

// Update the balance display
function updateBalanceDisplay() {
  balanceDisplay.textContent = `Balance: $${balance}`;
}

// Handle a box click
async function handleClick(btn) {
  const number = parseInt(btn.dataset.number);
  const index = shuffledNumbers.indexOf(number);
  const value = shuffledAmounts[index];

  if (clickCount === 0) {
    // First pick: choose your personal case
    personalCaseNumber = number;
    personalCaseValue = value;
    gameLog.personalCaseNumber = number;
    gameLog.personalCaseValue = value;
   btn.style.background = "linear-gradient(gold, goldenrod)";
   btn.style.border = "3px solid black";
   btn.style.color = "black";
   btn.style.boxShadow = "0 0 10px 2px rgba(255, 215, 0, 0.8)";
    btn.disabled = true;
    await showModal("Personal Case", `You picked case #${number} as your own.`, ["OK"]);
  } else {
    // Open a case
    btn.textContent = `$${value}`;
    btn.disabled = true;
    balance += value;
    valuesLeft = valuesLeft.filter((v) => v !== value);
    updateBalanceDisplay();

    gameLog.openedCases.push({ caseNumber: number, caseValue: value });


    // After opening cases, check if it's time for bank offer
    if ([4, 7, 10, 12, 14].includes(clickCount)) {
      await bankOffer();
    }

    // Final choice after last case opened
    if (clickCount === 14) {
      await finalChoice();
    }
  }

  clickCount++;
}

// Bank offer based on expected value * 0.8
async function bankOffer() {
  const expectedValue = valuesLeft.reduce((a, b) => a + b, 0) / valuesLeft.length;
  const offer = Math.floor(expectedValue * 0.8);

  const choice = await showModal(
    "Bank Offer",
    `Bank offers you $${offer}. Do you accept the deal?`,
    ["Accept", "Reject"]
  );

  gameLog.offers.push({ offer, choice });


  if (choice === "Accept") {
  gameLog.acceptedOffer = offer;
  gameLog.finalResult = offer;
  saveGameLog(gameLog);
    await showModal("Deal Accepted", `You accepted the deal for $${offer}!\nYour personal case had $${personalCaseValue}.`, ["OK"]);
    revealAllCases();
  } else {
    await showModal("Deal Rejected", "Deal rejected! Keep playing.", ["OK"]);
  }
}

// Reveal all unopened cases and disable buttons 
function revealAllCases() {
  const buttons = gameBoard.querySelectorAll("button");
  buttons.forEach((btn) => {
    if (!btn.disabled) {
      const number = parseInt(btn.dataset.number);
      const idx = shuffledNumbers.indexOf(number);
      btn.textContent = `$${shuffledAmounts[idx]}`;
      btn.disabled = true;
    }
  });
}

// Final choice: keep your case or swap with the last unopened case
async function finalChoice() {
  // Find last unopened case number and value
  let lastCaseNumber = null;
  let lastCaseValue = null;

  const buttons = gameBoard.querySelectorAll("button");
  buttons.forEach((btn) => {
    if (!btn.disabled) {
      lastCaseNumber = parseInt(btn.dataset.number);
    }
  });

  if (lastCaseNumber !== null) {
    const idx = shuffledNumbers.indexOf(lastCaseNumber);
    lastCaseValue = shuffledAmounts[idx];
  }

  const choice = await showModal(
    "Final Round",
    `Final round!\nDo you want to swap your case #${personalCaseNumber}  with case #${lastCaseNumber}?`,
    ["Swap", "Keep"]
  );

  if (choice === "Swap") {
  gameLog.finalResult = lastCaseValue;
    await showModal(
      "Swapped!",
      `You swapped!\nYour case had $${personalCaseValue}.\nCase #${lastCaseNumber} had $${lastCaseValue}.\nYou win $${lastCaseValue}!`,
      ["OK"]
    );
  } else {
  gameLog.finalResult = personalCaseValue;
    await showModal(
      "Kept Case",
      `You kept your case #${personalCaseNumber}.\nIt had $${personalCaseValue}.\nYou win $${personalCaseValue}!`,
      ["OK"]
    );
  }

  saveGameLog(gameLog);
  revealAllCases();
}


console.log(gameLog);
// Event listener for reset button
resetBtn.addEventListener("click", initGame);

// Start the game when page loads
window.onload = initGame;
