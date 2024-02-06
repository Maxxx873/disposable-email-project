
  import { ResponsiveContainer, AreaChart, XAxis, YAxis, Tooltip, Area } from "recharts";
import "./bigChartBox.scss";
  
  const data = [
    {
      name: "Sun",
      messages: 4000,
      domains: 2400,
      accounts: 2400,
    },
    {
      name: "Mon",
      messages: 3000,
      domains: 1398,
      accounts: 2210,
    },
    {
      name: "Tue",
      messages: 2000,
      domains: 9800,
      accounts: 2290,
    },
    {
      name: "Wed",
      messages: 2780,
      domains: 3908,
      accounts: 2000,
    },
    {
      name: "Thu",
      messages: 1890,
      domains: 4800,
      accounts: 2181,
    },
    {
      name: "Fri",
      messages: 2390,
      domains: 3800,
      accounts: 2500,
    },
    {
      name: "Sat",
      messages: 3490,
      domains: 4300,
      accounts: 2100,
    },
  ];
  
  const BigChartBox = () => {
    return (
      <div className="bigChartBox">
        <h1>Analytics</h1>
        <div className="chart">
        <ResponsiveContainer width="99%" height="100%">
            <AreaChart
              data={data}
              margin={{
                top: 10,
                right: 30,
                left: 0,
                bottom: 0,
              }}
            >
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip />
              <Area
                type="monotone"
                dataKey="messages"
                stackId="1"
                stroke="#8884d8"
                fill="#8884d8"
              />
              <Area
                type="monotone"
                dataKey="domains"
                stackId="1"
                stroke="#82ca9d"
                fill="#82ca9d"
              />
              <Area
                type="monotone"
                dataKey="accounts"
                stackId="1"
                stroke="#ffc658"
                fill="#ffc658"
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  };
  
  export default BigChartBox;