import React from 'react'
import {CloudinaryPickerCmp} from './components/CloudinaryPickerCmp';

export const CloudinaryPicker = ({field, id, value, editorContext, inputContext, onChange}) => {
    return (
        <>
            <CloudinaryPickerCmp field={field} value={value} editorContext={editorContext} onChange={onChange}/>
        </>

    )
}
