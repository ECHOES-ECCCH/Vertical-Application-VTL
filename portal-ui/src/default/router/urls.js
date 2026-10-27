import { include } from "named-urls";

export default {
	home: "/",
	auth: {
		login: "/login",
		emailToken: "/login/:token",
		register: "/register",
		profile: "/profile",
		account: "/account",
		remindPassword: "/remind-password",
		changePassword: include("/change-password", {
			send2mail: "",
			token: ":token",
		}),
		group: include("/group", {
			new: "",
			edit: ":id",
		}),
		admin: include("/admin", {
			projects: "projects/",
			users: "users/",
			userTranscriptions: "user-transcriptions/:id",
		}),
	},
	transcription: include("/transcription", {
		details: ":transcriptionId/",
		permissions: ":transcriptionId/permissions",
		layers: ":transcriptionId/layers",
		addLayer: ":transcriptionId/layers/addlayer",
		addText: ":transcriptionId/addtext",
		addTextToPage: ":transcriptionId/page/:pageId/addtext",
		viewer: ":transcriptionId/page/:pageId",
		new: "new",
		import: "import",
		edit: ":transcriptionId/edit",
		verification: ":transcriptionId/verification",
		indexes: ":transcriptionId/indexes",
	}),

	transcriptions: "/transcriptions",
	transcriptionsUser: "/my-transcriptions",

	accessibilityStatement: "/accessibility-statement",
	privacyPolicy: "/privacy-policy",
	contact: "/contact",
	regulations: "/regulations",
	howToTranscribe: "/how-to-transcribe",
	map: "/map",
};
