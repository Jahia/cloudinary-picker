import {registry} from '@jahia/ui-extender';
import {CloudinaryPickerDialog} from './CloudinaryPicker';
import svgCloudyLogo from './asset/logo.svg';
import i18next from 'i18next';

i18next.loadNamespaces('cloudinary-picker');

export default function () {
    // Load cloudy js
    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = 'https://media-library.cloudinary.com/global/all.js';
    script.async = true;
    document.getElementsByTagName('head')[0].appendChild(script);

    registry.add('callback', 'cloudinaryPickerSelectorType', {
        targets: ['jahiaApp-init:20'],
        callback: () => {
            const config = window.contextJsParameters.config?.cloudinary;
            registry.add('externalPickerConfiguration', 'cloudinary', {
                requireModuleInstalledOnSite: 'cloudinary-picker',
                pickerConfigs: config.applyOnPickers ? config.applyOnPickers.split(',').map(item => item.trim()) : ['image', 'file'],
                selectableTypes: ['cloudymix:cloudyAsset'],
                keyUrlPath: 'cloudinary',
                pickerInput: {
                    emptyLabel: 'cloudinary-picker:label.referenceCard.emptyLabel',
                    emptyIcon: svgCloudyLogo
                },
                pickerDialog: {
                    cmp: CloudinaryPickerDialog,
                    label: 'cloudinary-picker:label.selectorConfig.label',
                    description: 'cloudinary-picker:label.selectorConfig.description',
                    icon: svgCloudyLogo
                }
            });
            console.debug('%c CloudinaryPicker Editor Extensions  is activated', 'color: #3c8cba');
        }
    });
}
