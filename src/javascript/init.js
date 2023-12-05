import {registry} from '@jahia/ui-extender';
import {CloudinaryPickerDialog, useCloudinaryPickerInputData} from './CloudinaryPicker';
import svgCloudyLogo from './asset/logo.svg';
import i18next from 'i18next';

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
            registry.add('pickerConfiguration','cloudinary',{
                module:'cloudinary-picker',
                selectableTypes:['cloudymix:cloudyAsset'],
                keyUrlPath:'cloudinary',
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
            console.debug('%c CloudinaryPicker Editor Extensions  is activated', 'color: #3c8cba');

        }
    })
}
