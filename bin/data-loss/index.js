const { Client } = require('pg');
const pgPass = require('pgpass');
const jsondiffpatch = require('jsondiffpatch');

async function main() {
  const caseEvents = {
    '0000000000000000000': '00000000'
  };

  for (const [caseId, caseEventId] of Object.entries(caseEvents)) {
    console.log(`Processing case reference: ${caseId}, event ID: ${caseEventId}`);
    await reconstructData(caseEventId);
  }

}
async function reconstructData(caseEventId) {
  const client = await getClient();
  await client.connect();

  try {
    const { caseDataId, createdDate } = await getCaseDataIdFromEventId(client, caseEventId);
    const lastGoodData = await getBaseData(client, caseDataId, createdDate);
    const events = await getCaseDataEvents(client, caseDataId, createdDate);
    const baseData = jsondiffpatch.clone(lastGoodData);
    const originalNotes = lastGoodData['notes'] || [];
    baseData['cicCaseOrderList'] = events[0].data['cicCaseOrderList'];
    baseData['cicCaseOrderDynamicList'] = events[0].data['cicCaseOrderDynamicList'];

    for (let i = 1; i < events.length; i++) {
      const previousData = events[i - 1].data;
      const diff = jsondiffpatch.diff(previousData, events[i].data);
      // for (const key in diff) {
      //   console.log(`Applying diff ${key} ${i} to base data`);
      //   if (!baseData[key]) {
      //     console.log(`Key "${key}" not found in base data, adding it.`);
      //   }
      //   const smallDiff = { [key]: diff[key] };
      //   jsondiffpatch.patch(baseData, smallDiff);
      // }

      jsondiffpatch.patch(baseData, diff);
    }

    if (baseData['notes'].length != originalNotes.length) {
      baseData['notes'].push(...originalNotes);

      for (let i = 0; i < baseData['notes'].length; i++) {
        baseData['notes'][i].id = `${i + 1}`;
      }
    }
    console.log(JSON.stringify(lastGoodData));
    console.log(JSON.stringify(baseData));

  } catch (error) {
    console.error('Error reconstructing data:', error);
    throw error;
  } finally {
    await client.end();
  }
}

async function getClient() {
  const host = 'localhost';
  const port = 5440;
  const database = 'ccd_data_store';
  const user = 'DTS JIT Access ccd DB Reader SC';

  const password = await new Promise(resolve => {
    pgPass({ host, port, user, database }, resolve);
  });

  return new Client({
    host,
    port,
    database,
    user,
    password,
    ssl: {
      rejectUnauthorized: false
    }
  });
}

async function getCaseDataIdFromEventId(client, caseEventId) {
  const caseEventQuery = `
    SELECT case_data_id, created_date
    FROM case_event
    WHERE id = $1
  `;
  const caseEventResult = await client.query(caseEventQuery, [caseEventId]);
  if (caseEventResult.rows.length === 0) {
    throw new Error('Case event not found');
  }

  const { case_data_id, created_date } = caseEventResult.rows[0];

  return {
    caseDataId: case_data_id,
    createdDate: created_date,
  };
}

async function getBaseData(client, caseDataId, createdDate) {
  const baseDataQuery = `
    SELECT data AS base_data, created_date
    FROM case_event
    WHERE case_data_id = $1 AND created_date < $2
    ORDER BY created_date DESC
      LIMIT 1
  `;
  const rows = await client.query(baseDataQuery, [caseDataId, createdDate]);

  return rows.rows[0].base_data;
}

async function getCaseDataEvents(client, caseDataId, createdDate) {
  const eventSequenceQuery = `
    SELECT created_date, data
    FROM case_event
    WHERE case_data_id = $1 AND created_date >= $2
    ORDER BY created_date ASC
  `;
  const eventSequenceResult = await client.query(eventSequenceQuery, [caseDataId, createdDate]);

  return eventSequenceResult.rows;
}

main().catch(console.error)
