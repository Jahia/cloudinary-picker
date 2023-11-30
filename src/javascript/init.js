import {registry} from '@jahia/ui-extender';
// import {CloudinaryPicker} from './CloudinaryPicker';
import {CloudinaryPickerDialog, useCloudinaryPickerInputData} from './CloudinaryPickerDialog';
import svgCloudyLogo from './asset/logo.svg';
import i18next from 'i18next';
// import {registerCloudinaryPickerActions} from "./CloudinaryPicker/components/actions/registerPickerActions";

i18next.loadNamespaces('cloudinary-picker');

export default function () {
    //load cloudy js
    const script = document.createElement('script');
    script.type = 'text/javascript'
    script.src = 'https://media-library.cloudinary.com/global/all.js';
    script.async = true;
    document.getElementsByTagName('head')[0].appendChild(script)

    registry.add('callback', 'cloudinaryPickerSelectorType',{
        targets:['jahiaApp-init:20'],
        callback: () => {
            // registry.add('selectorType','CloudinaryPicker', {cmp: CloudinaryPicker, supportMultiple:false});
            console.debug('%c CloudinaryPicker Editor Extensions  is activated', 'color: #3c8cba');

            // registry.add('pickerDialog','CloudinaryPickerDialog', {cmp: CloudinaryPickerDialog});

            registry.add('pickerDialogSelectorConfiguration','cloudinary',{
                types: ['cloudymix:cloudyAsset'],
                label:'cloudinary-picker:label.selectorConfig.label',
                description: 'cloudinary-picker:label.selectorConfig.description',
                module:'cloudinary-picker',
                icon: svgCloudyLogo,
                pickerDialog: CloudinaryPickerDialog
            });

            registry.add('pickerConfiguration','cloudinary',{
                module:'cloudinary-picker',
                selectableTypes:['cloudymix:cloudyAsset'],
                pickerInput: {
                    emptyLabel: 'cloudinary-picker:label.referenceCard.emptyLabel',
                    emptyIcon: svgCloudyLogo,
                    usePickerInputData: useCloudinaryPickerInputData
                },
                pickerDialog:{
                    cmp:CloudinaryPickerDialog,
                    label:'cloudinary-picker:label.selectorConfig.label',
                    description: 'cloudinary-picker:label.selectorConfig.description',
                    icon: svgCloudyLogo,
                }
            });

            // registerCloudinaryPickerActions(registry);
        }
    })
}
