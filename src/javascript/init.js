import {registry} from '@jahia/ui-extender';
import {CloudinaryPicker} from './CloudinaryPicker';

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
            registry.add('selectorType','CloudinaryPicker', {cmp: CloudinaryPicker, supportMultiple:false});
            console.debug('%c CloudinaryPicker Editor Extensions  is activated', 'color: #3c8cba');
            registry.add('damSelectorConfiguration','CloudinaryPicker',{types: ['cloudymix:cloudyAsset'],label:'Cloudinary'});
        }
    })
}
