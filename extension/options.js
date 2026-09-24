const form = document.getElementById('options');

// TODO: fill the form with serverUrl and userId from chrome.storage.sync

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  // TODO: save serverUrl and userId to chrome.storage.sync. A server other than
  //  localhost:8080 needs its host permission first: chrome.permissions.request({ origins: [...] })
  //  (allowed by "optional_host_permissions" in manifest.json).
});
