import { GridColDef } from "@mui/x-data-grid";
import DataTable from "../../componetns/dataTable/DataTable"
import "./accounts.scss"
import { accountRows } from "../../data";
import { useState } from "react";
import Add from "../../componetns/add/Add";

const columns: GridColDef[] = [
  { field: "id", headerName: "ID", width: 90 },
  {
    field: "address",
    type: "string",
    headerName: "Address",
    width: 150,
  },
  {
    field: "quota",
    type: "string",
    headerName: "Quota",
    width: 150,
  },
  {
    field: "used",
    type: "string",
    headerName: "Used",
    width: 150,
  },
  {
    field: "isDeleted",
    type: "boolean",
    headerName: "Disabled",
    width: 200,
  },
  {
    field: "isDisabled",
    type: "boolean",
    headerName: "Disabled",
    width: 200,
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

const Accounts = () => {
  const [open, setOpen] = useState(false)
  return (
    <div className="accounts">
      <div className="info">
        <h1>Accounts</h1>
        <button onClick={() => setOpen(true)}>Add new Account</button>
      </div>
      <DataTable slug="accounts" columns={columns} rows={accountRows} />
      {open && <Add slug="account" columns={columns} setOpen={setOpen} />}
    </div>
  )
}

export default Accounts