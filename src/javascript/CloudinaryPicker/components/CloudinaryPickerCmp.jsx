import React from 'react'
// import {Typography} from '@jahia/moonstone';
import {Typography,Dialog,DialogTitle,DialogContent} from '@material-ui/core';

export const CloudinaryPickerCmp = () => {
    const [open,setOpen] = React.useState(false);
    const handleShow = () =>
        setOpen(true);

    const handleClose = () =>
        setOpen(false);

    const dialogConfig = {
        fullWidth: true,
        maxWidth: 'xl',
        dividers: true
    };

    return (
        <>
            <button
                data-sel-media-picker="empty"
                data-sel-field-picker-action="openPicker"
                // className={`${classes.add} ${isReadOnly ? classes.addReadOnly : ''}`}
                type="button"
                // aria-disabled={isReadOnly}
                // aria-labelledby={labelledBy}
                onClick={handleShow}
            >
                <div>
                    <Typography variant="omega" color="beta" component="span">
                        vide
                    </Typography>
                </div>
            </button>

            <Dialog
                open={open}
                fullWidth={dialogConfig.fullWidth}
                maxWidth={dialogConfig.maxWidth}
                onClose={handleClose}
            >
                <DialogTitle closeButton>
                    Cloudinary Picker
                </DialogTitle>
                <DialogContent dividers={dialogConfig.dividers}>
                    Picker !
                    {/*<Picker selectedItemId={fieldData?.wdenid}/>*/}

                </DialogContent>
            </Dialog>
        </>

    )
}
