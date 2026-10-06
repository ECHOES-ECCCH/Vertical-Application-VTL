import { reverse } from 'named-urls';

import url from "@/router/urls";

const updateUrl = (history, transcriptionId, pageId, q) => {
	let targetUrl = reverse(url.transcription.viewer, { transcriptionId: parseInt(transcriptionId), pageId: parseInt(pageId) });
	if(q && q != ""){
		targetUrl += "?q=" + q;
	}
	history.push(targetUrl);
};

export default updateUrl;