import React from 'react'
import {Button,Typography} from '@jahia/moonstone';
import {Dialog,DialogTitle,DialogContent} from '@material-ui/core';


export const CloudinaryPickerCmp = () => {
    // const [open,setOpen] = React.useState(false);
    const [widget,setWidget] = React.useState(null);

    React.useEffect( () => {
        if(!window.contextJsParameters.config?.cloudinary?.cloudName ||
            !window.contextJsParameters.config?.cloudinary?.apiKey){
            console.error("oups... cloudinary cloudName and apiKey are not configured! Please fill the cloudinary_picker_credentials.cfg file.")
        }else{
            if(window.cloudinary){
                setWidget(window.cloudinary.createMediaLibrary({
                    cloud_name: window.contextJsParameters.config.cloudinary.cloudName,
                    api_key: window.contextJsParameters.config.cloudinary.apiKey,
                }, {
                    insertHandler: (data) => {
                        console.log("cloudinary selected content : ",data);
                    }
                } ));
            }else{
                console.debug("oups... no window.cloudinary available !")
            }
        }
    },[])
    const handleShow = () =>
        widget.show();
        // setOpen(true);

    // const handleClose = () =>
    //     setOpen(false);

    // const dialogConfig = {
    //     fullWidth: true,
    //     maxWidth: 'xl',
    //     dividers: true
    // };


    return (
        <>
            {widget &&
            <Button

                label="Button"
                onClick={handleShow}
                size="default"
                variant="default"
            />



            // <button
            //     data-sel-media-picker="empty"
            //     data-sel-field-picker-action="openPicker"
            //     // className={`${classes.add} ${isReadOnly ? classes.addReadOnly : ''}`}
            //     type="button"
            //     // aria-disabled={isReadOnly}
            //     // aria-labelledby={labelledBy}
            //     onClick={handleShow}
            // >
            //     <div>
            //         {/*<Typography variant="omega" color="beta" component="span">*/}
            //             vide
            //         {/*</Typography>*/}
            //     </div>
            // </button>
            }
            {/*<Dialog*/}
            {/*    open={open}*/}
            {/*    fullWidth={dialogConfig.fullWidth}*/}
            {/*    maxWidth={dialogConfig.maxWidth}*/}
            {/*    onClose={handleClose}*/}
            {/*>*/}
            {/*    <DialogTitle closeButton>*/}
            {/*        Cloudinary Picker*/}
            {/*    </DialogTitle>*/}
            {/*    <DialogContent dividers={dialogConfig.dividers}>*/}
            {/*        Picker !*/}
            {/*        /!*<Picker selectedItemId={fieldData?.wdenid}/>*!/*/}

            {/*    </DialogContent>*/}
            {/*</Dialog>*/}
        </>

    )
}
