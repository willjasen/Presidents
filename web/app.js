const SUITS = [
  { symbol: '♣', key: 'clubs', red: false },
  { symbol: '♥', key: 'hearts', red: true },
  { symbol: '♠', key: 'spades', red: false },
  { symbol: '♦', key: 'diamonds', red: true },
];
const RANKS = ['3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K', 'A', '2'];
const PLAYER_NAMES = ['You', 'Jordan', 'Maya', 'Sam', 'Avery', 'Riley', 'Quinn'];
const GAME_STORAGE_KEY = 'presidents_saved_game_v1';
let players = PLAYER_NAMES.slice(0, 4);

const elements = {
  hand: document.querySelector('#hand'),
  pile: document.querySelector('#pile'),
  turnBanner: document.querySelector('#turn-banner'),
  lastPlay: document.querySelector('#last-play'),
  selectionHint: document.querySelector('#selection-hint'),
  playButton: document.querySelector('#play-button'),
  passButton: document.querySelector('#pass-button'),
  endGameButton: document.querySelector('#end-game-button'),
  newGameButton: document.querySelector('#new-game-button'),
  playerCount: document.querySelector('#player-count'),
  opponents: document.querySelector('#opponents'),
  yourCount: document.querySelector('#your-count'),
  toast: document.querySelector('#toast'),
  rulesDialog: document.querySelector('#rules-dialog'),
  authDialog: document.querySelector('#auth-dialog'),
  accountButton: document.querySelector('#account-button'),
  accountLabel: document.querySelector('#account-label'),
  authSignedOut: document.querySelector('#auth-signed-out'),
  authSignedIn: document.querySelector('#auth-signed-in'),
  authNote: document.querySelector('#auth-note'),
  leaderboardButton: document.querySelector('#leaderboard-button'),
  leaderboardDialog: document.querySelector('#leaderboard-dialog'),
  leaderboardTable: document.querySelector('#leaderboard-table'),
  roundResultsDialog: document.querySelector('#round-results-dialog'),
  roundResultsTable: document.querySelector('#round-results-table'),
  roundResultsIntro: document.querySelector('#round-results-intro'),
};

let game;
let selected = new Set();
let botTimer;
let soundOn = true;
let sessionToken = localStorage.getItem('presidents_session');
let profile = null;

function createDeck() {
  return RANKS.flatMap((rank, value) => SUITS.map((suit, suitIndex) => ({
    id: `${value}-${suitIndex}`, value, rank, suit: suit.symbol, suitKey: suit.key, red: suit.red,
  })));
}

function shuffle(cards) {
  const result = [...cards];
  for (let i = result.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [result[i], result[j]] = [result[j], result[i]];
  }
  return result;
}

function saveGame() {
  if (!game) return;
  try {
    localStorage.setItem(GAME_STORAGE_KEY, JSON.stringify({
      players,
      selected: [...selected],
      game: { ...game, passed: [...game.passed] },
    }));
  } catch { /* The game remains playable if storage is unavailable. */ }
}

function restoreGame() {
  try {
    const saved = JSON.parse(localStorage.getItem(GAME_STORAGE_KEY));
    const playerCount = saved?.players?.length;
    const storedGame = saved?.game;
    if (!Number.isInteger(playerCount) || playerCount < 2 || playerCount > 7
        || !Array.isArray(storedGame?.hands) || storedGame.hands.length !== playerCount
        || !storedGame.hands.every(Array.isArray)
        || !Number.isInteger(storedGame.current) || storedGame.current < 0 || storedGame.current >= playerCount
        || !['playing', 'finished'].includes(storedGame.status)) return false;

    const handIds = new Set(storedGame.hands.flat().map((card) => card?.id));
    if (handIds.has(undefined) || handIds.size !== storedGame.hands.flat().length) return false;

    players = PLAYER_NAMES.slice(0, playerCount);
    elements.playerCount.value = String(playerCount);
    game = {
      ...storedGame,
      passed: new Set((storedGame.passed || []).filter((index) => Number.isInteger(index) && index >= 0 && index < playerCount)),
      finishOrder: Array.isArray(storedGame.finishOrder) ? storedGame.finishOrder : [],
      lastCards: Array.isArray(storedGame.lastCards) ? storedGame.lastCards : [],
      id: storedGame.id || crypto.randomUUID(),
      startedAt: storedGame.startedAt || new Date().toISOString(),
      startingHands: Array.isArray(storedGame.startingHands) ? storedGame.startingHands : storedGame.hands.map((hand) => hand.map(cardForRecord)),
      history: Array.isArray(storedGame.history) ? storedGame.history : [],
      trickNumber: Number.isInteger(storedGame.trickNumber) ? storedGame.trickNumber : 1,
      recorded: Boolean(storedGame.recorded),
    };
    const humanCardIds = new Set(game.hands[0].map((card) => card.id));
    selected = new Set((saved.selected || []).filter((id) => humanCardIds.has(id)));
    render();
    queueBotTurn();
    return true;
  } catch {
    return false;
  }
}

function newGame() {
  clearTimeout(botTimer);
  const playerCount = Number(elements.playerCount.value);
  players = PLAYER_NAMES.slice(0, playerCount);
  const hands = Array.from({ length: playerCount }, () => []);
  shuffle(createDeck()).forEach((card, index) => hands[index % playerCount].push(card));
  hands.forEach((hand) => hand.sort((a, b) => a.value - b.value || a.suitKey.localeCompare(b.suitKey)));
  applyTitleExchange(hands);
  const openingPlayer = hands.findIndex((hand) => hand.some((card) => card.id === '0-0'));
  game = {
    hands, current: openingPlayer, cardsInPlay: 0, valueInPlay: -1, trickLeader: null,
    lastCards: [], lastPlayer: null, activity: null, passed: new Set(), finishOrder: [], firstPlay: true, status: 'playing',
    id: crypto.randomUUID(), startedAt: new Date().toISOString(),
    startingHands: hands.map((hand) => hand.map(cardForRecord)), history: [], trickNumber: 1, recorded: false,
  };
  selected = new Set();
  if (openingPlayer === 0) selected.add('0-0');
  saveGame();
  render();
  showToast(openingPlayer === 0 ? 'You hold the 3 of clubs. You lead!' : `${players[openingPlayer]} holds the 3 of clubs.`);
  queueBotTurn();
}

function applyTitleExchange(hands) {
  const order = game?.status === 'finished' ? game.finishOrder : null;
  if (!order || order.length !== players.length) return;
  const exchanges = players.length >= 4 ? [[0, players.length - 1, 2], [1, players.length - 2, 1]] : [[0, players.length - 1, 1]];
  exchanges.forEach(([giverPlace, receiverPlace, count]) => {
    const giver = order[giverPlace]; const receiver = order[receiverPlace];
    const outgoing = hands[giver].slice(-count); const incoming = hands[receiver].slice(0, count);
    hands[giver] = hands[giver].filter((card) => !outgoing.includes(card)).concat(incoming);
    hands[receiver] = hands[receiver].filter((card) => !incoming.includes(card)).concat(outgoing);
  });
  hands.forEach((hand) => hand.sort((a, b) => a.value - b.value || a.suitKey.localeCompare(b.suitKey)));
}

function endGame() {
  const shouldReset = game.status === 'finished'
    || window.confirm('End this game and start a new one?');
  if (shouldReset) newGame();
}

function cardMarkup(card, { button = false, selectedCard = false, index = 0 } = {}) {
  const tag = button ? 'button' : 'div';
  const attrs = button
    ? `type="button" data-card-id="${card.id}" aria-label="${card.rank} of ${card.suitKey}" aria-pressed="${selectedCard}"`
    : `aria-label="${card.rank} of ${card.suitKey}" style="--angle:${(index - 1.5) * 4}deg"`;
  return `<${tag} class="card ${card.red ? 'red' : 'black'} ${selectedCard ? 'selected' : ''}" ${attrs}><span class="rank">${card.rank}</span><span class="suit">${card.suit}</span></${tag}>`;
}

function playedCardPictures(cards) {
  return `<span class="played-card-pictures" aria-hidden="true">${cards.map((card) =>
    `<span class="played-card-picture ${card.red ? 'red' : ''}"><span class="mini-rank">${card.rank}</span><span class="mini-suit">${card.suit}</span></span>`
  ).join('')}</span>`;
}

function cardForRecord(card) {
  return { id: card.id, rank: card.rank, suit: card.suit, value: card.value };
}

function recordAction(playerIndex, action, cards, handSizeAfter, pileCountBefore, pileRankBefore) {
  if (!Array.isArray(game.history)) game.history = [];
  game.history.push({
    sequence: game.history.length + 1,
    trickNumber: game.trickNumber || 1,
    playerSeat: playerIndex,
    action,
    cards: cards.map(cardForRecord),
    handSizeAfter,
    pileCountBefore,
    pileRankBefore: pileRankBefore >= 0 ? pileRankBefore : null,
    occurredAt: new Date().toISOString(),
  });
}

function renderOpponents() {
  const seatLayouts = {
    1: [[50, 28]],
    2: [[18, 43], [82, 43]],
    3: [[14, 48], [50, 28], [86, 48]],
    4: [[13, 57], [27, 29], [73, 29], [87, 57]],
    5: [[11, 59], [24, 30], [50, 26], [76, 30], [89, 59]],
    6: [[10, 62], [15, 34], [37, 27], [63, 27], [85, 34], [90, 62]],
  };
  const seats = seatLayouts[players.length - 1];
  elements.opponents.innerHTML = players.slice(1).map((name, offset) => {
    const index = offset + 1;
    const [x, y] = seats[offset];
    const side = x > 50 ? 'right' : 'left';
    return `<div class="opponent" data-player="${index}" data-side="${side}" style="--seat-x:${x}%;--seat-y:${y}%">
      <div class="avatar" aria-hidden="true">${name[0]}</div>
      <div class="player-copy"><strong>${name}</strong><span>${countLabel(game.hands[index].length)}</span></div>
    </div>`;
  }).join('');
}

function render() {
  const humanTurn = game.status === 'playing' && game.current === 0;
  const roundNumber = game.trickNumber || 1;
  const roundStart = game.status === 'playing' && game.cardsInPlay === 0;
  elements.hand.innerHTML = game.hands[0].map((card) => cardMarkup(card, {
    button: true, selectedCard: selected.has(card.id),
  })).join('');
  elements.yourCount.textContent = countLabel(game.hands[0].length);

  renderOpponents();
  document.querySelectorAll('.opponent').forEach((opponent) => {
    const index = Number(opponent.dataset.player);
    const finishedAt = game.finishOrder.indexOf(index);
    const cardCount = countLabel(game.hands[index].length);
    opponent.querySelector('.player-copy span').textContent = finishedAt >= 0
      ? placeName(finishedAt + 1)
      : game.passed.has(index) ? `Passed · ${cardCount}` : cardCount;
    opponent.classList.toggle('active', game.current === index && game.status === 'playing');
  });

  if (game.lastCards.length) {
    elements.pile.innerHTML = game.lastCards.map((card, index) => cardMarkup(card, { index })).join('');
    const activity = game.activity || `${players[game.lastPlayer]} played ${playLabel(game.lastCards)}.`;
    elements.lastPlay.innerHTML = `${playedCardPictures(game.lastCards)}<span>${activity}</span>`;
  } else {
    const leaderText = game.current === 0 ? 'You lead' : `${players[game.current]} leads`;
    elements.pile.innerHTML = `<div class="empty-pile round-start-pile"><span>♣</span><strong>Round ${roundNumber}</strong><small>${leaderText}</small></div>`;
    const leadMessage = game.firstPlay
      ? `${game.current === 0 ? 'You have the 3 of clubs and lead' : `${players[game.current]} has the 3 of clubs and leads`} the first trick.`
      : `Round ${roundNumber} begins — ${game.current === 0 ? 'you lead' : `${players[game.current]} leads`}.`;
    elements.lastPlay.textContent = leadMessage;
  }

  elements.turnBanner.classList.toggle('new-round', roundStart);
  if (game.status === 'finished') {
    const place = game.finishOrder.indexOf(0) + 1;
    elements.turnBanner.textContent = place === 1 ? 'You’re President!' : `You finished ${placeName(place)}`;
  } else if (roundStart) {
    elements.turnBanner.textContent = `Round ${roundNumber} · ${game.current === 0 ? 'You lead' : `${players[game.current]} leads`}`;
  } else if (humanTurn) elements.turnBanner.textContent = 'Your turn';
  else elements.turnBanner.textContent = `${players[game.current]} is thinking…`;

  const validation = validateSelection();
  elements.playButton.disabled = !humanTurn || !validation.valid;
  elements.passButton.disabled = !humanTurn || game.cardsInPlay === 0;
  elements.playButton.innerHTML = `${selected.size === 1 ? 'Play card' : `Play ${selected.size || ''} cards`.trim()} <span aria-hidden="true">→</span>`;
  elements.selectionHint.textContent = selectionMessage(validation, humanTurn);
}

function countLabel(count) { return `${count} ${count === 1 ? 'card' : 'cards'}`; }
function placeName(place) {
  if (place === 1) return 'President';
  if (place === players.length) return 'Scum';
  if (players.length >= 4 && place === 2) return 'Vice President';
  if (players.length >= 4 && place === players.length - 1) return 'Vice Scum';
  return 'Citizen';
}
function playLabel(cards) { return cards.length === 1 ? `a ${cards[0].rank}` : `${cards.length} ${cards[0].rank}s`; }
function selectedCards() { return game.hands[0].filter((card) => selected.has(card.id)); }

function validateCards(cards) {
  if (!cards.length) return { valid: false, message: 'Choose at least one card' };
  if (!cards.every((card) => card.value === cards[0].value)) return { valid: false, message: 'Choose cards of the same rank' };
  if (game.firstPlay && !cards.some((card) => card.id === '0-0')) return { valid: false, message: 'The first play must include the 3 of clubs' };
  if (game.cardsInPlay && cards.length < game.cardsInPlay) return { valid: false, message: `Play at least ${game.cardsInPlay} ${game.cardsInPlay === 1 ? 'card' : 'cards'}` };
  if (game.cardsInPlay && cards[0].value < game.valueInPlay) return { valid: false, message: `Play ${RANKS[game.valueInPlay]}s or higher` };
  return { valid: true, message: `${playLabel(cards)} ready` };
}

function validateSelection() { return validateCards(selectedCards()); }
function selectionMessage(validation, humanTurn) {
  if (game.status === 'finished') return 'Start a new game to play again.';
  if (!humanTurn) return `Waiting for ${players[game.current]}`;
  return validation.message;
}

function playCards(playerIndex, cards) {
  if (game.status !== 'playing' || playerIndex !== game.current) return;
  const validation = validateCards(cards);
  if (!validation.valid) return showToast(validation.message);
  const pileCountBefore = game.cardsInPlay;
  const pileRankBefore = game.valueInPlay;
  const ids = new Set(cards.map((card) => card.id));
  game.hands[playerIndex] = game.hands[playerIndex].filter((card) => !ids.has(card.id));
  recordAction(playerIndex, 'play', cards, game.hands[playerIndex].length, pileCountBefore, pileRankBefore);
  game.cardsInPlay = cards.length;
  game.valueInPlay = cards[0].value;
  game.lastCards = cards;
  game.lastPlayer = playerIndex;
  const clearsWithTwo = cards[0].value === RANKS.length - 1;
  game.activity = `${players[playerIndex]} played ${playLabel(cards)}${clearsWithTwo ? ' and cleared the trick' : ''}.`;
  game.trickLeader = playerIndex;
  game.firstPlay = false;
  game.passed.delete(playerIndex);
  selected.clear();
  if (!game.hands[playerIndex].length) game.finishOrder.push(playerIndex);
  if (game.finishOrder.length === players.length - 1) {
    game.finishOrder.push(players.findIndex((_, index) => !game.finishOrder.includes(index)));
    game.status = 'finished';
    game.completedAt = new Date().toISOString();
    finishGame();
  } else advanceTurn();
  saveGame();
  render();
  queueBotTurn();
}

function pass(playerIndex) {
  if (game.status !== 'playing' || playerIndex !== game.current || !game.cardsInPlay) return;
  recordAction(playerIndex, 'pass', [], game.hands[playerIndex].length, game.cardsInPlay, game.valueInPlay);
  game.passed.add(playerIndex);
  game.activity = `${players[playerIndex]} passed.`;
  showToast(`${players[playerIndex]} passed.`);
  advanceTurn();
  saveGame();
  render();
  queueBotTurn();
}

function isFinished(index) { return game.finishOrder.includes(index); }
function shouldClearTrick() {
  return game.valueInPlay === RANKS.length - 1
    || players.every((_, index) => index === game.trickLeader || isFinished(index) || game.passed.has(index));
}
function nextActive(from) {
  for (let step = 1; step <= players.length; step += 1) {
    const index = (from + step) % players.length;
    if (!isFinished(index)) return index;
  }
  return from;
}
function nextEligible(from) {
  for (let step = 1; step <= players.length; step += 1) {
    const index = (from + step) % players.length;
    if (!isFinished(index) && !game.passed.has(index)) return index;
  }
  return from;
}
function advanceTurn() {
  if (shouldClearTrick()) {
    const leader = game.trickLeader;
    game.cardsInPlay = 0;
    game.valueInPlay = -1;
    game.lastCards = [];
    game.lastPlayer = null;
    game.passed.clear();
    game.trickNumber = (game.trickNumber || 1) + 1;
    game.current = isFinished(leader) ? nextActive(leader) : leader;
    game.activity = `Round ${game.trickNumber} begins — ${game.current === 0 ? 'you lead' : `${players[game.current]} leads`}.`;
    showToast(game.activity);
  } else game.current = nextEligible(game.current);
}

function chooseBotPlay(index) {
  const byRank = new Map();
  game.hands[index].forEach((card) => {
    if (!byRank.has(card.value)) byRank.set(card.value, []);
    byRank.get(card.value).push(card);
  });
  if (game.firstPlay) return byRank.get(0) || [];
  if (!game.cardsInPlay) return byRank.get([...byRank.keys()].sort((a, b) => a - b)[0]);
  const playableGroups = [...byRank.entries()]
    .filter(([value, group]) => value >= game.valueInPlay && group.length >= game.cardsInPlay)
    .sort(([a], [b]) => a - b);
  if (playableGroups.length) return playableGroups[0][1].slice(0, game.cardsInPlay);
  return [];
}
function queueBotTurn() {
  clearTimeout(botTimer);
  if (game.status !== 'playing' || game.current === 0) return;
  const delay = game.activity?.endsWith('passed.')
    ? 1250 + Math.random() * 350
    : 650 + Math.random() * 450;
  botTimer = setTimeout(() => {
    const index = game.current;
    const cards = chooseBotPlay(index);
    if (cards.length) playCards(index, cards); else pass(index);
  }, delay);
}

async function finishGame() {
  const place = game.finishOrder.indexOf(0) + 1;
  showToast(place === 1 ? 'You’re the President!' : `You finished as ${placeName(place)}.`);
  showRoundResults();
  await syncFinishedGame();
}

function showRoundResults() {
  elements.roundResultsIntro.textContent = 'These titles determine the card exchange in the next game.';
  elements.roundResultsTable.innerHTML = `<div class="leaderboard-row header"><span>#</span><span>Player</span><span class="leaderboard-stat">Title</span></div>${game.finishOrder.map((index, place) => `<div class="leaderboard-row ${index === 0 ? 'is-you' : ''}"><span class="leaderboard-rank">${place + 1}</span><span class="leaderboard-name">${escapeHtml(players[index])}${index === 0 ? ' · You' : ''}</span><span class="leaderboard-stat">${placeName(place + 1)}</span></div>`).join('')}`;
  elements.roundResultsDialog.showModal();
}

async function syncFinishedGame() {
  if (!sessionToken || game?.status !== 'finished' || game.recorded) return;
  const place = game.finishOrder.indexOf(0) + 1;
  try {
    await api('save-game', {
      gameId: game.id,
      startedAt: game.startedAt,
      completedAt: game.completedAt || new Date().toISOString(),
      playerCount: players.length,
      finishPlace: place,
      finishOrder: game.finishOrder,
      rules: { version: 1, threesLow: true, twosHigh: true, twosClear: true, equalRanksAllowed: true, responseCount: 'at-least' },
      players: players.map((name, seat) => ({
        seat,
        name: seat === 0 && profile?.username ? profile.username : name,
        type: seat === 0 ? 'human' : 'computer',
        startingHand: game.startingHands?.[seat] || [],
        finishPlace: game.finishOrder.indexOf(seat) + 1,
      })),
      actions: game.history || [],
    });
    game.recorded = true;
    saveGame();
    await loadProfile();
  } catch {
    showToast('Game saved on this device; database sync will retry after sign-in.');
  }
}
function showToast(message) {
  elements.toast.textContent = message;
  elements.toast.classList.add('show');
  clearTimeout(showToast.timer);
  showToast.timer = setTimeout(() => elements.toast.classList.remove('show'), 2200);
}

elements.hand.addEventListener('click', (event) => {
  const button = event.target.closest('[data-card-id]');
  if (!button || game.current !== 0 || game.status !== 'playing') return;
  const id = button.dataset.cardId;
  if (selected.has(id)) {
    selected.delete(id);
  } else {
    const card = game.hands[0].find((handCard) => handCard.id === id);
    const selectedCard = selectedCards()[0];
    if (selectedCard && card && card.value !== selectedCard.value) {
      showToast('Choose cards of the same rank');
      return;
    }
    selected.add(id);
  }
  saveGame();
  render();
});
elements.playButton.addEventListener('click', () => playCards(0, selectedCards()));
elements.passButton.addEventListener('click', () => pass(0));
elements.endGameButton.addEventListener('click', endGame);
elements.newGameButton.addEventListener('click', () => game.status === 'finished' ? showRoundResults() : newGame());
document.querySelector('#start-next-game').addEventListener('click', () => { elements.roundResultsDialog.close(); newGame(); });
document.querySelector('#rules-button').addEventListener('click', () => elements.rulesDialog.showModal());
document.querySelector('#rules-close').addEventListener('click', () => elements.rulesDialog.close());
document.querySelector('#rules-done').addEventListener('click', () => elements.rulesDialog.close());
document.querySelector('#sound-button').addEventListener('click', (event) => {
  soundOn = !soundOn;
  event.currentTarget.setAttribute('aria-label', `Turn sound ${soundOn ? 'off' : 'on'}`);
  event.currentTarget.style.opacity = soundOn ? '1' : '.45';
});

elements.accountButton.addEventListener('click', () => elements.authDialog.showModal());
document.querySelector('#auth-close').addEventListener('click', () => elements.authDialog.close());
document.querySelector('#signout-button').addEventListener('click', () => {
  localStorage.removeItem('presidents_session');
  sessionToken = null; profile = null; renderProfile();
});
elements.leaderboardButton.addEventListener('click', async () => {
  elements.leaderboardDialog.showModal();
  elements.leaderboardTable.innerHTML = '<p class="leaderboard-empty">Loading standings…</p>';
  try {
    const result = await api('leaderboard');
    renderLeaderboard(result.players);
  } catch (error) {
    elements.leaderboardTable.innerHTML = `<p class="leaderboard-empty">${escapeHtml(error.message)}</p>`;
  }
});
document.querySelector('#leaderboard-close').addEventListener('click', () => elements.leaderboardDialog.close());

async function api(action, body = {}) {
  const response = await fetch('/.netlify/functions/auth', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...(sessionToken ? { Authorization: `Bearer ${sessionToken}` } : {}) },
    body: JSON.stringify({ action, ...body }),
  });
  const isJson = response.headers.get('content-type')?.includes('application/json');
  const result = isJson ? await response.json().catch(() => ({})) : {};
  if (!response.ok) {
    if (!isJson || [404, 405, 501].includes(response.status)) {
      throw new Error('Passkey accounts are unavailable on the static preview. Start the Netlify local preview with Neon connected.');
    }
    throw new Error(result.error || 'Something went wrong. Please try again.');
  }
  return result;
}
function setAuthStatus(message, error = false) {
  elements.authNote.textContent = message;
  elements.authNote.classList.toggle('error', error);
}
async function loadWebAuthn() {
  if (!window.SimpleWebAuthnBrowser) throw new Error('The passkey helper did not load. Refresh and try again.');
  return window.SimpleWebAuthnBrowser;
}

document.querySelector('#register-button').addEventListener('click', async () => {
  const username = document.querySelector('#display-name').value.trim();
  if (username.length < 2) return setAuthStatus('Enter at least two characters for your name.', true);
  try {
    setAuthStatus('Your device will ask you to create a passkey…');
    const { startRegistration } = await loadWebAuthn();
    const start = await api('register-options', { username });
    const credential = await startRegistration({ optionsJSON: start.options });
    acceptSession(await api('register-verify', { challengeId: start.challengeId, credential }));
  } catch (error) { setAuthStatus(error.message, true); }
});

document.querySelector('#signin-button').addEventListener('click', async () => {
  try {
    setAuthStatus('Choose your Presidents passkey…');
    const { startAuthentication } = await loadWebAuthn();
    const start = await api('login-options');
    const credential = await startAuthentication({ optionsJSON: start.options });
    acceptSession(await api('login-verify', { challengeId: start.challengeId, credential }));
  } catch (error) { setAuthStatus(error.message, true); }
});

function acceptSession(result) {
  sessionToken = result.token;
  localStorage.setItem('presidents_session', sessionToken);
  profile = result.user;
  renderProfile();
  setAuthStatus('Signed in.');
  showToast(`Welcome, ${profile.username}!`);
  syncFinishedGame();
}
async function loadProfile() {
  if (!sessionToken) return renderProfile();
  try { profile = (await api('me')).user; }
  catch { localStorage.removeItem('presidents_session'); sessionToken = null; profile = null; }
  renderProfile();
}
function renderProfile() {
  elements.authSignedOut.hidden = Boolean(profile);
  elements.authSignedIn.hidden = !profile;
  elements.accountButton.classList.toggle('signed-in', Boolean(profile));
  elements.leaderboardButton.hidden = !profile;
  elements.accountLabel.textContent = profile ? profile.username : 'Sign in';
  if (profile) {
    document.querySelector('#profile-name').textContent = profile.username;
    document.querySelector('#profile-avatar').textContent = profile.username[0].toUpperCase();
    document.querySelector('#profile-record').textContent = `${profile.gamesPlayed || 0} games · ${profile.wins || 0} wins`;
  }
}

function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, (character) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;',
  })[character]);
}

function renderLeaderboard(players) {
  if (!players.length) {
    elements.leaderboardTable.innerHTML = '<p class="leaderboard-empty">No completed games yet.</p>';
    return;
  }
  elements.leaderboardTable.innerHTML = `
    <div class="leaderboard-row header"><span>#</span><span>Player</span><span class="leaderboard-stat">Wins</span><span class="leaderboard-stat">Losses</span><span class="leaderboard-stat leaderboard-rate">Rate</span></div>
    ${players.map((player, index) => `
      <div class="leaderboard-row ${profile?.id === player.id ? 'is-you' : ''}">
        <span class="leaderboard-rank">${index + 1}</span>
        <span class="leaderboard-name">${escapeHtml(player.username)}${profile?.id === player.id ? ' · You' : ''}</span>
        <span class="leaderboard-stat">${player.wins}</span>
        <span class="leaderboard-stat">${player.losses}</span>
        <span class="leaderboard-stat leaderboard-rate">${player.winRate}%</span>
      </div>`).join('')}`;
}

if (!restoreGame()) newGame();
loadProfile();
