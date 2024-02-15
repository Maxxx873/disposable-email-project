import { useEffect, useState } from "react"
import Add from "../../componetns/add/Add"
import DataTable from "../../componetns/dataTable/DataTable"
import { DefaultApi } from "../../admin-axios-client"
import { Domain } from "../../admin-axios-client/models/domain"
import "./domains.scss"
import { GridColDef } from "@mui/x-data-grid"
import moment from 'moment'

const adminAPI = new DefaultApi();

const columns: GridColDef[] = [
  { field: "id", headerName: "ID", width: 250 },
  {
    field: "domain",
    type: "string",
    headerName: "Domain",
    width: 200,
  },
  {
    field: "isActive",
    headerName: "Active",
    width: 100,
    type: "boolean",
  },
  {
    field: "isPrivate",
    headerName: "Private",
    width: 100,
    type: "boolean",
  },
  {
    field: "createdAt",
    headerName: "Created At",
    width: 200,
    type: "string",
  },
  {
    field: "updatedAt",
    headerName: "Updated At",
    width: 200,
    type: "string",
  },
];


const Domains = () => {
  const [open, setOpen] = useState(false);
  const [domains, setDomains] = useState<Domain[]>([]);

/*   useEffect(() => {
    adminAPI.getDomainCollection()
      .then((response) => {
        setDomains(response.data);
      })
      .catch((error) => {
        console.error("Error fetching data:", error);
      });
  }, []); */
  
  useEffect(() => {
    adminAPI.getDomainCollection()
      .then((response) => {
        const rawItems = response.data as Array<Domain>;
        const parsedData = rawItems.map((domain) => ({
          ...domain,
          createdAt: moment.utc(domain.createdAt!).local().format('YYYY-MM-DD HH:mm'),
          updatedAt: moment.utc(domain.updatedAt!).local().format('YYYY-MM-DD HH:mm')
        }))
        setDomains(parsedData);
      })
      .catch((error) => {
        console.error("Error fetching data:", error);
      });
  }, []);


  return (
    <div className="domains">
      <div className="info">
        <h1>Domains</h1>
        <button onClick={() => setOpen(true)}>Add new Domain</button>
      </div>
      <DataTable slug="domains" columns={columns} rows={domains} />
      {open && <Add slug="domain" columns={columns} setOpen={setOpen} />}
    </div>
  )
}

export default Domains