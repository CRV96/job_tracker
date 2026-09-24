// Injected into the job posting page by popup.js. The value of the last expression
// in this file is what popup.js receives, so the whole script is one function call.
(() => {
  // TODO: 1. Read <script type="application/ld+json"> blocks and find the one whose "@type" is
  //          "JobPosting" (it can also sit inside an array or an "@graph"). Map title,
  //          hiringOrganization.name, description, jobLocation, baseSalary and employmentType,
  //          and keep the whole object as rawPayload.
  //       2. Without JSON-LD, fall back to Open Graph tags (og:title, og:description).
  //       3. Without those, fall back to the page title, as below.
  return {
    title: document.title,
    company: null,
    description: null,
    link: window.location.href,
    location: null,
    salary: null,
    employmentType: null,
    rawPayload: null,
  };
})();
