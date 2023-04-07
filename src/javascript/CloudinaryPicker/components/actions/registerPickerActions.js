import React from 'react';
import {Edit, Cancel, MoreVert} from '@jahia/moonstone';
import {unsetFieldAction} from './unsetFieldAction';
import {replaceAction} from './replaceAction';

export const registerCloudinaryPickerActions = registry => {
    registry.add('action', 'content-editor/field/CloudinaryPicker', registry.get('action', 'menuAction'), {
        buttonIcon: <MoreVert/>,
        buttonLabel: 'label.contentEditor.edit.action.fieldMoreOptions',
        menuTarget: 'content-editor/field/CloudinaryPickerActions',
        menuItemProps: {
            isShowIcons: true
        }
    });

    registry.add('action', 'replaceCloudinaryContent', replaceAction, {
        buttonIcon: <Edit/>,
        buttonLabel: 'content-editor:label.contentEditor.edit.fields.actions.replace',
        targets: ['content-editor/field/CloudinaryPickerActions:1']
    });

    registry.add('action', 'unsetFieldActionCloudinaryPicker', unsetFieldAction, {
        buttonIcon: <Cancel/>,
        buttonLabel: 'content-editor:label.contentEditor.edit.fields.actions.clear',
        targets: ['content-editor/field/CloudinaryPickerActions:2']
    });
};
