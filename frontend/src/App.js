import React, {useState} from 'react';

function App() {
  const [name, setName] = useState('');
  const [scheduleType, setScheduleType] = useState('ONE_TIME');
  const [scheduledTime, setScheduledTime] = useState('');
  const [cron, setCron] = useState('');
  const [ownerId, setOwnerId] = useState('1');

  const submit = async () => {
    const body = {
      name,
      type: 'SIMPLE_TASK',
      payload: 'Hello from frontend',
      scheduleType,
      scheduledTime: scheduleType==='ONE_TIME' ? new Date(scheduledTime).toISOString() : null,
      cronExpression: scheduleType==='CRON' ? cron : null,
      ownerId: parseInt(ownerId)
    };
    const res = await fetch('/api/jobs', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify(body)
    });
    const data = await res.json();
    alert('Created job id: ' + data.id);
  };

  return (
    <div style={{padding:20}}>
      <h2>Chronos - Create Job</h2>
      <div>
        <label>Owner ID: <input value={ownerId} onChange={e=>setOwnerId(e.target.value)} /></label>
      </div>
      <div>
        <label>Name: <input value={name} onChange={e=>setName(e.target.value)} /></label>
      </div>
      <div>
        <label>Schedule Type:
          <select value={scheduleType} onChange={e=>setScheduleType(e.target.value)}>
            <option value="ONE_TIME">One-time</option>
            <option value="CRON">Cron (recurring)</option>
          </select>
        </label>
      </div>
      {scheduleType==='ONE_TIME' && (
        <div>
          <label>Scheduled time: <input type="datetime-local" onChange={e=>setScheduledTime(e.target.value)} /></label>
        </div>
      )}
      {scheduleType==='CRON' && (
        <div>
          <label>Cron expression: <input value={cron} onChange={e=>setCron(e.target.value)} /></label>
        </div>
      )}
      <button onClick={submit}>Create Job</button>
    </div>
  );
}

export default App;
