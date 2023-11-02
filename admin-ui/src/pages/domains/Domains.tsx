import { useState } from "react"
import Add from "../../componetns/add/Add"
import DataTable from "../../componetns/dataTable/DataTable"
import { domains } from "../../data"
import "./domains.scss"
import { GridColDef } from "@mui/x-data-grid"

const columns: GridColDef[] = [
  { field: "id", headerName: "ID", width: 90 },
  {
    field: "domain",
    type: "string",
    headerName: "Domain",
    width: 250,
  },
  {
    field: "isActive",
    headerName: "Active",
    width: 200,
    type: "boolean",
  },
  {
    field: "isPrivate",
    headerName: "Private",
    width: 200,
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
  const [open, setOpen] = useState(false)
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