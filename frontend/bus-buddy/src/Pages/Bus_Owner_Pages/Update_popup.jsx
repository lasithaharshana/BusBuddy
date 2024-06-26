import React from "react";
import Dialog from "@mui/material/Dialog";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import IconButton from "@mui/material/IconButton";
import CloseIcon from "@mui/icons-material/Close";

function Update_popup(props) {
  const { title, children, openPopup, setOpenPopup } = props;
  return (
    <Dialog open={openPopup}>
      <DialogTitle>
        <div className="d-flex flex-row align-items-center justify-content-between">
          <div>{title}</div>
          <IconButton
            style={{ color: "red" }}
            className="mx-2"
            aria-label="delete"
            onClick={() => setOpenPopup(false)}
          >
            <CloseIcon />
          </IconButton>
        </div>
      </DialogTitle>
      <DialogContent dividers>{children}</DialogContent>
    </Dialog>
  );
}

export default Update_popup;
