#!/usr/bin/env node

/**
 * Builds a map:
 * {
 *   "<CaseEventID>": {
 *     "sourceFile": "src/main/java/...",
 *     "pages": {
 *       "<PageID>": {
 *         "sourceFile": "src/main/java/...",
 *         "fields": [
 *           {
 *             "caseFieldId": "<CaseFieldID>",
 *             "DisplayContext": "...",
 *             "FieldShowCondition": "...",
 *             "PageShowCondition": "..."
 *           }
 *         ]
 *       }
 *     }
 *   }
 * }
 *
 * Filters fields where:
 * 1) FieldShowCondition is present (non-empty), and
 * 2) Either:
 *    - (Main case) DisplayContext is MANDATORY or OPTIONAL, or
 *    - (Edge case) DisplayContext is READONLY and PageShowCondition is present (non-empty)
 *
 * Additionally, produces an HTML document which displays this.
 */

const fs = require("node:fs");
const path = require("node:path");

const inputDirArg = process.argv[2] || "build/definitions/CaseEventToFields";
const projectRoot = path.resolve(__dirname, "..");
const javaRoot = path.join(projectRoot, "src", "main", "java");

function hasValue(value) {
  return typeof value === "string" ? value.trim() !== "" : value != null;
}

function getRowEventId(row) {
  return row.CaseEventID || "__MISSING_EVENT_ID__";
}

function getRowPageId(row) {
  return row.PageID || "__MISSING_PAGE_ID__";
}

function getEventPageKey(row) {
  return `${getRowEventId(row)}::${getRowPageId(row)}`;
}

function buildPageShowConditionMap(rows) {
  const map = new Map();

  for (const row of rows) {
    if (hasValue(row.PageShowCondition)) {
      const key = getEventPageKey(row);
      if (!map.has(key)) {
        map.set(key, String(row.PageShowCondition).trim());
      }
    }
  }

  return map;
}

function classifyRow(row, hasEffectivePageShowCondition) {
  const fieldId = String(row.CaseFieldID || "").trim();
  if (fieldId.startsWith("Label")) return null;

  const displayContext = (row.DisplayContext || "").trim().toUpperCase();
  const retainHiddenValue = (row.RetainHiddenValue || "").trim().toUpperCase();
  const hasFieldShowCondition = hasValue(row.FieldShowCondition);

  // No issue
  if (retainHiddenValue === "Y") return null;
  if (!hasFieldShowCondition) return null;

  // Mainline
  if (displayContext === "OPTIONAL" || displayContext === "MANDATORY") {
    return "mainline";
  }

  // Edge
  if (displayContext === "READONLY" && hasEffectivePageShowCondition) {
    return "edge";
  }

  // No issue
  return null;
}

function sortByOrder(a, b) {
  const pageA = Number.isFinite(Number(a.PageDisplayOrder))
    ? Number(a.PageDisplayOrder)
    : Number.MAX_SAFE_INTEGER;
  const pageB = Number.isFinite(Number(b.PageDisplayOrder))
    ? Number(b.PageDisplayOrder)
    : Number.MAX_SAFE_INTEGER;

  if (pageA !== pageB) return pageA - pageB;

  const fieldA = Number.isFinite(Number(a.PageFieldDisplayOrder))
    ? Number(a.PageFieldDisplayOrder)
    : Number.MAX_SAFE_INTEGER;
  const fieldB = Number.isFinite(Number(b.PageFieldDisplayOrder))
    ? Number(b.PageFieldDisplayOrder)
    : Number.MAX_SAFE_INTEGER;

  if (fieldA !== fieldB) return fieldA - fieldB;

  return String(a.CaseFieldID).localeCompare(String(b.CaseFieldID));
}

function toRepoRelative(filePath) {
  return path.relative(projectRoot, filePath).split(path.sep).join("/");
}

function listFilesRecursively(dir, extension) {
  const out = [];

  function walk(currentDir) {
    const entries = fs.readdirSync(currentDir, { withFileTypes: true });
    for (const entry of entries) {
      const fullPath = path.join(currentDir, entry.name);
      if (entry.isDirectory()) {
        walk(fullPath);
      } else if (entry.isFile() && fullPath.endsWith(extension)) {
        out.push(fullPath);
      }
    }
  }

  if (fs.existsSync(dir)) {
    walk(dir);
  }

  out.sort((a, b) => a.localeCompare(b));
  return out;
}

function parseEventConstants(javaFiles) {
  const constantToEventId = {};
  const constantRegex = /public\s+static\s+final\s+String\s+([A-Z0-9_]+)\s*=\s*"([^"]+)"/g;

  for (const filePath of javaFiles) {
    const content = fs.readFileSync(filePath, "utf8");
    let match;
    while ((match = constantRegex.exec(content)) !== null) {
      const [, constantName, eventId] = match;
      constantToEventId[constantName] = eventId;
    }
  }

  return constantToEventId;
}

function buildSourceMaps(javaFiles, constantToEventId) {
  const eventSourceMap = {};
  const pageSourceMap = {};

  const eventLiteralRegex = /\.event\(\s*"([^"]+)"\s*\)/g;
  const eventConstantRegex = /\.event\(\s*([A-Z][A-Z0-9_]+)\s*\)/g;
  const pageRegex = /\.page\(\s*"([^"]+)"/g;

  for (const filePath of javaFiles) {
    const content = fs.readFileSync(filePath, "utf8");
    const relativeFilePath = toRepoRelative(filePath);

    let match;

    while ((match = pageRegex.exec(content)) !== null) {
      const pageId = match[1];
      if (!pageSourceMap[pageId]) {
        pageSourceMap[pageId] = relativeFilePath;
      }
    }

    while ((match = eventLiteralRegex.exec(content)) !== null) {
      const eventId = match[1];
      if (!eventSourceMap[eventId]) {
        eventSourceMap[eventId] = relativeFilePath;
      }
    }

    while ((match = eventConstantRegex.exec(content)) !== null) {
      const constantName = match[1];
      const eventId = constantToEventId[constantName];
      if (eventId && !eventSourceMap[eventId]) {
        eventSourceMap[eventId] = relativeFilePath;
      }
    }
  }

  return { eventSourceMap, pageSourceMap };
}

function main() {
  const inputDir = path.resolve(process.cwd(), inputDirArg);

  if (!fs.existsSync(inputDir)) {
    console.error(`Input directory not found: ${inputDir}`);
    process.exit(1);
  }

  const javaFiles = listFilesRecursively(javaRoot, ".java");
  const constantToEventId = parseEventConstants(javaFiles);
  const { eventSourceMap, pageSourceMap } = buildSourceMaps(javaFiles, constantToEventId);

  const files = fs
    .readdirSync(inputDir)
    .filter((f) => f.endsWith(".json"))
    .sort((a, b) => a.localeCompare(b));

  const eventMap = {};

  for (const fileName of files) {
    const fullPath = path.join(inputDir, fileName);
    let rows;

    try {
      const raw = fs.readFileSync(fullPath, "utf8");
      rows = JSON.parse(raw);
      if (!Array.isArray(rows)) {
        console.error(`Skipping ${fileName}: expected JSON array`);
        continue;
      }
    } catch (err) {
      console.error(`Skipping ${fileName}: ${err.message}`);
      continue;
    }

    const pageShowConditionMap = buildPageShowConditionMap(rows);

    const matchingRows = rows
      .map((row) => {
        const key = getEventPageKey(row);
        const effectivePageShowCondition = hasValue(row.PageShowCondition)
          ? String(row.PageShowCondition).trim()
          : (pageShowConditionMap.get(key) || null);

        return {
          row,
          conditionType: classifyRow(row, hasValue(effectivePageShowCondition)),
          effectivePageShowCondition,
        };
      })
      .filter((item) => item.conditionType !== null)
      .sort((a, b) => sortByOrder(a.row, b.row));

    for (const item of matchingRows) {
      const row = item.row;
      const eventId = row.CaseEventID || "__MISSING_EVENT_ID__";
      const pageId = row.PageID || "__MISSING_PAGE_ID__";
      const fieldId = row.CaseFieldID || "__MISSING_FIELD_ID__";

      if (!eventMap[eventId]) {
        eventMap[eventId] = {
          sourceFile: eventSourceMap[eventId] || null,
          pages: {},
        };
      }

      if (!eventMap[eventId].pages[pageId]) {
        eventMap[eventId].pages[pageId] = {
          sourceFile: pageSourceMap[pageId] || null,
          fields: [],
          _fieldIds: new Set(),
        };
      }

      const page = eventMap[eventId].pages[pageId];
      if (!page._fieldIds.has(fieldId)) {
        page._fieldIds.add(fieldId);
        page.fields.push({
          caseFieldId: fieldId,
          DisplayContext: row.DisplayContext || null,
          FieldShowCondition: row.FieldShowCondition || null,
          PageShowCondition: item.effectivePageShowCondition,
          conditionType: item.conditionType, // "mainline" or "edge"
        });
      }
    }
  }

  const sorted = {};
  for (const eventId of Object.keys(eventMap).sort((a, b) => a.localeCompare(b))) {
    sorted[eventId] = {
      sourceFile: eventMap[eventId].sourceFile,
      pages: {},
    };

    const pageIds = Object.keys(eventMap[eventId].pages).sort((a, b) => a.localeCompare(b));
    for (const pageId of pageIds) {
      const page = eventMap[eventId].pages[pageId];
      sorted[eventId].pages[pageId] = {
        sourceFile: page.sourceFile,
        fields: page.fields,
      };
    }
  }

  // process.stdout.write(`${JSON.stringify(sorted, null, 2)}\n`);
  //
  // const outputFormat = (process.argv[3] || "json").toLowerCase(); // json | html
  // const output = outputFormat === "html" ? renderHtml(sorted) : JSON.stringify(sorted, null, 2);
  // process.stdout.write(`${output}\n`);
  fs.mkdirSync("build", { recursive: true })
  fs.writeFileSync("build/report.json", JSON.stringify(sorted, null, 2) + "\n", "utf8");
  fs.writeFileSync("build/report.html", renderHtml(sorted) + "\n", "utf8");
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function renderHtml(report) {
  const eventIds = Object.keys(report).sort((a, b) => a.localeCompare(b));

  const eventsHtml = eventIds.map((eventId) => {
    const eventObj = report[eventId];
    const pageIds = Object.keys(eventObj.pages || {}).sort((a, b) => a.localeCompare(b));
    const totalFields = pageIds.reduce((sum, pageId) => {
      return sum + ((eventObj.pages[pageId].fields || []).length);
    }, 0);

    const pagesHtml = pageIds.map((pageId) => {
      const pageObj = eventObj.pages[pageId];
      const fields = pageObj.fields || [];

      const rows = fields.map((f, idx) => {
        const fieldKey = `${eventId}::${pageId}::${f.caseFieldId}`;
        const rowClass = f.conditionType === "edge" ? "field-row edge" : "field-row";
        const radioName = `status-${fieldKey}`;
        const noChangeId = `${radioName}-no-change-${idx}`;
        const fixedItId = `${radioName}-fixed-it-${idx}`;

        return `
  <tr
    class="${rowClass}"
    data-field-key="${escapeHtml(fieldKey)}"
    data-event-id="${escapeHtml(eventId)}"
    data-page-id="${escapeHtml(pageId)}"
  >
  <td><input type="checkbox" class="field-check" aria-label="Mark ${escapeHtml(f.caseFieldId)} done" /></td>
  <td class="status-cell">
    <label><input type="radio" class="field-status" name="${escapeHtml(radioName)}" id="${escapeHtml(noChangeId)}" value="no-change" /> No change needed</label>
    <label><input type="radio" class="field-status" name="${escapeHtml(radioName)}" id="${escapeHtml(fixedItId)}" value="fixed-it" /> Needs fix</label>
  </td>
  <td><code>${escapeHtml(f.caseFieldId)}</code></td>
  <td>${escapeHtml(f.DisplayContext)}</td>
  <td><code>${escapeHtml(f.FieldShowCondition)}</code></td>
  <td><code>${escapeHtml(f.PageShowCondition)}</code></td>
  <td class="comment-cell">
    <input
      type="text"
      class="field-comment"
      placeholder="Add comment..."
      aria-label="Comment for ${escapeHtml(f.caseFieldId)}"
    />
  </td>
</tr>
`;
      }).join("");

      return `
        <details class="page" data-event-id="${escapeHtml(eventId)}" data-page-id="${escapeHtml(pageId)}">
          <summary>
            <strong>${escapeHtml(pageId)}</strong>
            <span class="meta">(${fields.length} field${fields.length === 1 ? "" : "s"})</span>
            <span class="progress" data-page-progress="${escapeHtml(eventId)}::${escapeHtml(pageId)}">0/${fields.length} done</span>
            <span class="src">${escapeHtml(pageObj.sourceFile || "source not found")}</span>
          </summary>
          <div class="table-wrap">
            <table>
              <thead>
<tr>
  <th>Done</th>
  <th>Status</th>
  <th>CaseFieldID</th>
  <th>DisplayContext</th>
  <th>FieldShowCondition</th>
  <th>PageShowCondition</th>
  <th>Comment</th>
</tr>
              </thead>
              <tbody>${rows}</tbody>
            </table>
          </div>
        </details>
      `;
    }).join("");

    return `
      <details class="event" data-event-id="${escapeHtml(eventId)}">
        <summary>
          <strong>${escapeHtml(eventId)}</strong>
          <span class="meta">(${pageIds.length} page${pageIds.length === 1 ? "" : "s"}, ${totalFields} field${totalFields === 1 ? "" : "s"})</span>
          <span class="progress" data-event-progress="${escapeHtml(eventId)}">0/${totalFields} done</span>
          <span class="src">${escapeHtml(eventObj.sourceFile || "source not found")}</span>
        </summary>
        ${pagesHtml}
      </details>
    `;
  }).join("");

  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <title>Hidden Field Analysis</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif; margin: 20px; color: #222; }
    h1 { margin-bottom: 6px; }
    .hint { color: #444; margin-bottom: 8px; }
    .rules { margin: 6px 0 0 18px; padding: 0; }
    .rules li { margin: 2px 0; }
    tr.field-row td { background: #fdecec; }      /* red = mainline unchecked */
    tr.field-row.edge td { background: #fff4db; } /* amber = edge unchecked */
    tr.field-row.done td { background: #e8f5e9; } /* green when checked */
    .counter { margin-bottom: 14px; font-size: 15px; }
    .counter strong { color: #b42318; }
    .comment-cell input.field-comment {
      width: 100%;
      min-width: 220px;
      box-sizing: border-box;
      font-size: 12px;
      padding: 4px 6px;
    }
    details { border: 1px solid #ddd; border-radius: 8px; padding: 8px 10px; margin: 10px 0; background: #fff; }
    details.page { margin-left: 18px; }
    summary { cursor: pointer; list-style: none; }
    summary::-webkit-details-marker { display: none; }
    summary strong { margin-right: 8px; }
    .meta { color: #555; margin-right: 10px; }
    .src { color: #0b57d0; font-family: ui-monospace, Menlo, monospace; font-size: 12px; }
    .table-wrap { margin-top: 10px; overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; font-size: 13px; }
    th, td { border: 1px solid #e5e5e5; padding: 6px 8px; vertical-align: top; text-align: left; }
    th { background: #f7f7f7; }
    code { background: #f6f8fa; padding: 1px 4px; border-radius: 4px; }
    .progress {
      color: #0f5132;
      font-weight: 600;
      margin-right: 10px;
    }
    .state-tools { margin-bottom: 14px; display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
    #import-state { display: none; }
    .import-label, #export-state {
      border: 1px solid #c7c7c7; border-radius: 6px; padding: 6px 10px; background: #fff; cursor: pointer;
      font-size: 13px;
    }
    .import-message { color: #555; font-size: 13px; }
    .status-cell label { margin-right: 10px; white-space: nowrap; font-size: 12px; }
    .status-cell input[type="radio"] { margin-right: 4px; }
  </style>
</head>
<body>
  <h1>Hidden Field Analysis</h1>
  <div class="hint">
    <strong>Classification rules:</strong>
    <ul class="rules">
      <li><strong>No issue</strong> (excluded): RetainHiddenValue is set, or FieldShowCondition is missing, or it does not match the rules below.</li>
      <li><strong>Mainline</strong>: DisplayContext is OPTIONAL or MANDATORY, FieldShowCondition is present, and RetainHiddenValue is not set.</li>
      <li><strong>Edge</strong>: DisplayContext is READONLY, FieldShowCondition is present, PageShowCondition is present, and RetainHiddenValue is not set.</li>
      <li><strong>Page-level propagation</strong>: if any field in an Event+Page has PageShowCondition, that PageShowCondition is treated as present for all fields in that same Event+Page.</li>
    </ul>
  </div>
  <div class="counter">Unchecked fields: <strong id="unchecked-count">0</strong></div>
  <div class="state-tools">
    <button id="export-state" type="button">Export state JSON</button>
    <label for="import-state" class="import-label">Import state JSON</label>
    <input id="import-state" type="file" accept="application/json,.json" />
    <span id="import-message" class="import-message"></span>
  </div>
  ${eventsHtml}

  <script>
    (function () {
      const STORAGE_KEY = "hidden-field-analysis:state:v3";
      const rows = Array.from(document.querySelectorAll("tr.field-row"));
      const countEl = document.getElementById("unchecked-count");
      const exportBtn = document.getElementById("export-state");
      const importInput = document.getElementById("import-state");
      const importMessage = document.getElementById("import-message");

      const allowedStatuses = new Set(["no-change", "fixed-it"]);
      const rowByKey = new Map(rows.map((row) => [row.getAttribute("data-field-key"), row]));

      const pageDetails = Array.from(document.querySelectorAll("details.page"));
      const eventDetails = Array.from(document.querySelectorAll("details.event"));

      function computeStats() {
        const pageStats = new Map();
        const eventStats = new Map();

        rows.forEach((row) => {
          const eventId = row.getAttribute("data-event-id");
          const pageId = row.getAttribute("data-page-id");
          const pageKey = eventId + "::" + pageId;
          const checked = row.querySelector(".field-check").checked;

          if (!pageStats.has(pageKey)) {
            pageStats.set(pageKey, { total: 0, done: 0, unchecked: 0 });
          }
          if (!eventStats.has(eventId)) {
            eventStats.set(eventId, { total: 0, done: 0, unchecked: 0 });
          }

          const p = pageStats.get(pageKey);
          const e = eventStats.get(eventId);

          p.total += 1;
          e.total += 1;

          if (checked) {
            p.done += 1;
            e.done += 1;
          } else {
            p.unchecked += 1;
            e.unchecked += 1;
          }
        });

        return { pageStats, eventStats };
      }

      function updateProgressLabels() {
        const { pageStats, eventStats } = computeStats();

        pageDetails.forEach((detailsEl) => {
          const eventId = detailsEl.getAttribute("data-event-id");
          const pageId = detailsEl.getAttribute("data-page-id");
          const key = eventId + "::" + pageId;
          const stat = pageStats.get(key) || { total: 0, done: 0, unchecked: 0 };
          const label = detailsEl.querySelector("[data-page-progress]");
          if (label) {
            label.textContent = stat.done +"/"+stat.total + " done";
          }
        });

        eventDetails.forEach((detailsEl) => {
          const eventId = detailsEl.getAttribute("data-event-id");
          const stat = eventStats.get(eventId) || { total: 0, done: 0, unchecked: 0 };
          const label = detailsEl.querySelector("[data-event-progress]");
          if (label) {
            label.textContent = stat.done +"/"+stat.total + " done";
          }
        });

        return { pageStats, eventStats };
      }

      function expandSectionsWithUncheckedOnLoad() {
        const { pageStats, eventStats } = updateProgressLabels();

        pageDetails.forEach((detailsEl) => {
          const eventId = detailsEl.getAttribute("data-event-id");
          const pageId = detailsEl.getAttribute("data-page-id");
          const key = eventId + "::" + pageId;
          const stat = pageStats.get(key);
          if (stat && stat.unchecked > 0) {
            detailsEl.open = true;
          }
        });

        eventDetails.forEach((detailsEl) => {
          const eventId = detailsEl.getAttribute("data-event-id");
          const stat = eventStats.get(eventId);
          if (stat && stat.unchecked > 0) {
            detailsEl.open = true;
          }
        });
      }

      function normaliseEntry(entry) {
        if (typeof entry === "boolean") {
          return { checked: entry, status: "no-change", comment: "" };
        }
        if (!entry || typeof entry !== "object") {
          return { checked: false, status: "no-change", comment: "" };
        }

        const checked = entry.checked === true;
        const status = allowedStatuses.has(entry.status) ? entry.status : "no-change";
        const comment = typeof entry.comment === "string" ? entry.comment : "";

        return { checked, status, comment };
      }

      let stateByField = {};
      try {
        const parsed = JSON.parse(localStorage.getItem(STORAGE_KEY) || "{}");
        if (parsed && typeof parsed === "object" && parsed.fields && typeof parsed.fields === "object") {
          stateByField = parsed.fields;
        } else if (parsed && typeof parsed === "object") {
          // backward-compat if previous shape was direct map
          stateByField = parsed;
        }
      } catch (e) {
        stateByField = {};
      }

      function saveState() {
        localStorage.setItem(
          STORAGE_KEY,
          JSON.stringify({ version: 2, fields: stateByField })
        );
      }

      function updateCounter() {
        let unchecked = 0;
        rows.forEach((row) => {
          const checkbox = row.querySelector(".field-check");
          if (!checkbox.checked) unchecked += 1;
        });
        countEl.textContent = String(unchecked);
      }

      function applyRowState(row, entry) {
        const safe = normaliseEntry(entry);
        const checkbox = row.querySelector(".field-check");
        checkbox.checked = safe.checked;
        row.classList.toggle("done", safe.checked);

        const radios = row.querySelectorAll(".field-status");
        radios.forEach((radio) => {
          radio.checked = radio.value === safe.status;
        });

        const commentInput = row.querySelector(".field-comment");
        if (commentInput) {
          commentInput.value = safe.comment;
        }
      }

      function getRowState(row) {
        const checkbox = row.querySelector(".field-check");
        const selected = row.querySelector(".field-status:checked");
        const commentInput = row.querySelector(".field-comment");

        return {
          checked: checkbox.checked,
          status: selected ? selected.value : "no-change",
          comment: commentInput ? commentInput.value : "",
        };
      }

      function setMessage(text, isError) {
        importMessage.textContent = text;
        importMessage.style.color = isError ? "#b42318" : "#555";
      }

      rows.forEach((row) => {
        const key = row.getAttribute("data-field-key");
        const saved = normaliseEntry(stateByField[key]);

        applyRowState(row, saved);
        stateByField[key] = saved;

        const checkbox = row.querySelector(".field-check");
        checkbox.addEventListener("change", () => {
          stateByField[key] = getRowState(row);
          applyRowState(row, stateByField[key]);
          saveState();
          updateCounter();
          updateProgressLabels();
        });

        const radios = row.querySelectorAll(".field-status");
        radios.forEach((radio) => {
          radio.addEventListener("change", () => {
            stateByField[key] = getRowState(row);
            saveState();
            updateCounter();
            updateProgressLabels();
          });
        });

        const commentInput = row.querySelector(".field-comment");
        if (commentInput) {
          commentInput.addEventListener("input", () => {
            stateByField[key] = getRowState(row);
            saveState();
          });
        }
      });

      exportBtn.addEventListener("click", () => {
        const payload = {
          version: 2,
          exportedAt: new Date().toISOString(),
          fields: stateByField,
        };
        const blob = new Blob([JSON.stringify(payload, null, 2)], { type: "application/json" });
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "hidden-field-analysis-state.json";
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
        setMessage("State exported.", false);
      });

      importInput.addEventListener("change", async (event) => {
        const file = event.target.files && event.target.files[0];
        if (!file) return;

        try {
          const text = await file.text();
          const parsed = JSON.parse(text);

          const importedFields = parsed && typeof parsed === "object"
            ? (parsed.fields && typeof parsed.fields === "object" ? parsed.fields : parsed)
            : null;

          if (!importedFields || typeof importedFields !== "object") {
            throw new Error("Invalid JSON shape");
          }

          // Only apply keys that exist in current report
          rowByKey.forEach((row, key) => {
            const next = normaliseEntry(importedFields[key]);
            stateByField[key] = next;
            applyRowState(row, next);
          });

          saveState();
          updateCounter();
          updateProgressLabels();
          setMessage("State imported.", false);
        } catch (err) {
          setMessage("Import failed: invalid JSON.", true);
        } finally {
          importInput.value = "";
        }
      });

      updateCounter();
      updateProgressLabels();
      expandSectionsWithUncheckedOnLoad();
    })();
</script>
</body>
</html>`;
}

main();

