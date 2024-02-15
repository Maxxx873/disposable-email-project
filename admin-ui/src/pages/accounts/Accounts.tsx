import { useEffect, useState } from "react"
import { GridColDef } from "@mui/x-data-grid";
import DataTable from "../../componetns/dataTable/DataTable"
import "./accounts.scss"
import { DefaultApi } from "../../admin-axios-client"
import { Account } from "../../admin-axios-client/models/account"
import Add from "../../componetns/add/Add";
import moment from 'moment'

const adminAPI = new DefaultApi();

const columns: GridColDef[] = [
  { field: "id", headerName: "ID", width: 250 },
  {
    field: "address",
    type: "string",
    headerName: "Address",
    width: 200,
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
    width: 100,
  },
  {
    field: "isDeleted",
    type: "boolean",
    headerName: "Deleted",
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
  const [accountRows, setAccounts] = useState<Account[]>([]);

  useEffect(() => {
    adminAPI.getAccountCollection()
      .then((response) => {
        const rawItems = response.data as Array<Account>;
        const parsedData = rawItems.map((account) => ({
          ...account,
          createdAt: moment.utc(account.createdAt!).local().format('YYYY-MM-DD HH:mm'),
          updatedAt: moment.utc(account.updatedAt!).local().format('YYYY-MM-DD HH:mm')
        }))
        setAccounts(parsedData);
      })
      .catch((error) => {
        console.error("Error fetching data:", error);
      });
  }, []);

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