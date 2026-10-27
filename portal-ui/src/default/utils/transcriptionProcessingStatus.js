import api from "@/services/api";

const getTranscriptionProcessingStatus = (transcriptionId, setTranscriptionProcessingStatus,setTranscriptionProcessingStatusRequest ) => {
	setTranscriptionProcessingStatusRequest(false);
	api.get(`/transcriptions/${transcriptionId}/processing-status`).then(({data}) => {
		setTranscriptionProcessingStatus(data.status === 'NOT_EXISTS' ? null : data);
		setTranscriptionProcessingStatusRequest(true);
	}).catch(() => {
		//console.log(error);
	});
};

export default getTranscriptionProcessingStatus;