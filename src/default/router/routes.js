import Login from "@Pages/Auth/Login/Login";
import Register from "@Pages/Auth/Register/Register";
import RemindPassword from "@Pages/Auth/RemindPassword/RemindPassword";
import ChangePassword from "@Pages/Auth/ChangePassword/ChangePassword";

import Account from "@Pages/Auth/Account/Account";
import Profile from "@Pages/Auth/Profile/Profile";
import Group from "@Pages/Auth/Group/Group";

import DashboardProjects from "@Pages/Admin/Dashboard/DashboardProjects";
import DashboardUsers from "@Pages/Admin/Dashboard/DashboardUsers";
import UserTranscriptions from "@Pages/Admin/UserTranscriptions/UserTranscriptions";

import Transcriptions from "@Pages/Transcriptions/Transcriptions";

import Viewer from "@Pages/Viewer/Viewer";
import TranscriptionPermissions from "@Pages/Transcription/TranscriptionPermissions/TranscriptionPermissions";
import TranscriptionLayers from "@Pages/Transcription/TranscriptionLayers/TranscriptionLayers";
import TranscriptionAddLayer from "@Pages/Transcription/TranscriptionAddLayer/TranscriptionAddLayer";

import TranscriptionNew from "@Pages/Transcription/TranscriptionNew/TranscriptionNew";
import TranscriptionImport from "@Pages/Transcription/TranscriptionImport/TranscriptionImport";

import TranscriptionDetails from "@Pages/Transcription/TranscriptionDetails/TranscriptionDetails";
import TranscriptionAddText from "@Pages/Transcription/TranscriptionAddText/TranscriptionAddText";
import TranscriptionVerification from "@Pages/Transcription/TranscriptionVerification/TranscriptionVerification";
import TranscriptionIndexes from "@Pages/Transcription/TranscriptionIndexes/TranscriptionIndexes";

import Home from "@Pages/Home/Home";
import StaticContent from "@Pages/StaticContent/StaticContent";
import Contact from "@Pages/Contact/Contact";
import Map from "@Pages/Map/Map";
import HowToTranscribe from "@Pages/HowToTranscribe/TranscriptionHelpPage";

import error404 from "@Pages/Error/Error404";

import AppHeader from "@Templates/header/AppHeader/AppHeader";
import AuthHeader from "@Templates/header/AuthHeader/AuthHeader";
import ViewerHeader from "@Templates/header/ViewerHeader/ViewerHeader";

import AppFooter from "@Templates/footer/AppFooter/AppFooter";
import ViewerFooter from "@Templates/footer/ViewerFooter/ViewerFooter";

import url from "@/router/urls";

export default [
	{
		id: "login",
		path: url.auth.login,
		exact: true,
		isProtected: false,
		components: { header: AuthHeader, main: Login, footer: AppFooter },
	},
	{
		id: "emailToken",
		path: url.auth.emailToken,
		exact: true,
		isProtected: false,
		components: { header: AuthHeader, main: Login, footer: AppFooter },
	},
	{
		id: "register",
		path: url.auth.register,
		exact: true,
		isProtected: false,
		components: { header: AuthHeader, main: Register, footer: AppFooter },
	},
	{
		id: "createGroup",
		path: url.auth.group.new,
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: Group, footer: AppFooter },
	},
	{
		id: "editGroup",
		path: "/group/:id",
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: Group, footer: AppFooter },
	},
	{
		id: "account",
		path: url.auth.account,
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: Account, footer: AppFooter },
	},
	{
		id: "profile",
		path: url.auth.profile,
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: Profile, footer: AppFooter },
	},
	{
		id: "remindPassword",
		path: url.auth.remindPassword,
		exact: true,
		isProtected: false,
		components: { header: AuthHeader, main: RemindPassword, footer: AppFooter },
	},
	{
		id: "changePassword",
		path: url.auth.changePassword.token,
		exact: true,
		isProtected: false,
		components: { header: AuthHeader, main: ChangePassword, footer: AppFooter },
	},
	{
		id: "adminProjcts",
		path: url.auth.admin.projects,
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: DashboardProjects, footer: AppFooter },
	},
	{
		id: "adminUsers",
		path: url.auth.admin.users,
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: DashboardUsers, footer: AppFooter },
	},
	{
		id: "adminUserTranscriptions",
		path: url.auth.admin.userTranscriptions,
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: UserTranscriptions, footer: AppFooter },
	},
	{
		id: "transcriptionNew",
		path: url.transcription.new,
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: TranscriptionNew, footer: AppFooter },
	},
	{
		id: "transcriptionImport",
		path: url.transcription.import,
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: TranscriptionImport, footer: AppFooter },
	},
	{
		id: "transcriptionEdit",
		path: url.transcription.edit,
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: TranscriptionNew, footer: AppFooter },
	},
	{
		id: "viewer",
		path: url.transcription.viewer,
		exact: true,
		isProtected: false,
		components: { header: ViewerHeader, main: Viewer, footer: ViewerFooter },
	},
	{
		id: "transcriptionDetails",
		path: url.transcription.details,
		exact: true,
		isProtected: false,
		components: { header: AppHeader, main: TranscriptionDetails, footer: AppFooter },
	},
	{
		id: "transcriptionPermissions",
		path: url.transcription.permissions,
		exact: true,
		isProtected: false,
		components: { header: AppHeader, main: TranscriptionPermissions, footer: AppFooter },
	},
	{
		id: "transcriptionLayers",
		path: url.transcription.layers,
		exact: true,
		isProtected: false,
		components: { header: AppHeader, main: TranscriptionLayers, footer: AppFooter },
	},
	{
		id: "transcriptionAddLayer",
		path: url.transcription.addLayer,
		exact: true,
		isProtected: false,
		components: { header: AppHeader, main: TranscriptionAddLayer, footer: AppFooter },
	},
	{
		id: "transcriptionAddText",
		path: url.transcription.addText,
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: TranscriptionAddText, footer: AppFooter },
	},
	{
		id: "transcriptionAddTextToPage",
		path: url.transcription.addTextToPage,
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: TranscriptionAddText, footer: AppFooter },
	},
	{
		id: "transcriptionVerification",
		path: url.transcription.verification,
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: TranscriptionVerification, footer: AppFooter },
	},
	{
		id: "transcriptionIndexes",
		path: url.transcription.indexes,
		exact: true,
		isProtected: false,
		components: { header: AppHeader, main: TranscriptionIndexes, footer: AppFooter },
	},
	{
		id: "transcriptions",
		path: url.transcriptions,
		exact: true,
		isProtected: false,
		components: { header: AppHeader, main: Transcriptions, footer: AppFooter },
	},
	{
		id: "transcriptionsUser",
		path: url.transcriptionsUser,
		exact: true,
		isProtected: true,
		components: { header: AppHeader, main: Transcriptions, footer: AppFooter },
	},
	{
		id: "accessibility-statement",
		path: url.accessibilityStatement,
		exact: true,
		isProtected: false,
		components: { header: AppHeader, main: StaticContent, footer: AppFooter },
	},
	{
		id: "privacy-policy",
		path: url.privacyPolicy,
		exact: true,
		isProtected: false,
		components: { header: AppHeader, main: StaticContent, footer: AppFooter },
	},
	{
		id: "contact",
		path: url.contact,
		exact: true,
		isProtected: false,
		components: { header: AppHeader, main: Contact, footer: AppFooter },
	},
	{
		id: "regulations",
		path: url.regulations,
		exact: true,
		isProtected: false,
		components: { header: AppHeader, main: StaticContent, footer: AppFooter },
	},
	{
		id: "map",
		path: url.map,
		exact: true,
		isProtected: false,
		components: { header: AppHeader, main: Map, footer: AppFooter },
	},
	{
		id: "howToTranscribe",
		path: url.howToTranscribe,
		exact: true,
		isProtected: false,
		components: { header: AppHeader, main: HowToTranscribe, footer: AppFooter },
	},
	{
		id: "home",
		path: url.home,
		exact: true,
		isProtected: false,
		components: { header: AppHeader, main: Home, footer: AppFooter },
	},
	{
		id: "error404",
		isProtected: false,
		components: { header: AppHeader, main: error404, footer: AppFooter },
	},
];
