const DEFAULT_SERVER_URL = 'http://localhost:8080';

/** Runs extract.js inside the current tab and returns what it found. */
async function extractFromActiveTab() {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
  const [injection] = await chrome.scripting.executeScript({
    target: { tabId: tab.id },
    files: ['extract.js'],
  });
  return injection.result;
}

/** @param {'SAVE' | 'FAVORITE'} action */
async function capture(action, job) {
  const { serverUrl = DEFAULT_SERVER_URL, userId } = await chrome.storage.sync.get(['serverUrl', 'userId']);
  // TODO: POST { userId, action, ...job } as JSON to `${serverUrl}/api/captures`, then show
  //  success or the error in #status. If userId isn't set yet, point the user to the options page.
  throw new Error('Not implemented yet');
}

const job = await extractFromActiveTab();
// TODO: show the extracted fields in #preview so the user can check them before saving

document.getElementById('save').addEventListener('click', () => capture('SAVE', job));
document.getElementById('favorite').addEventListener('click', () => capture('FAVORITE', job));
