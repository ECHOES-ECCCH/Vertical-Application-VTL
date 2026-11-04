import { useEffect, useMemo, useState } from "react";
import { useSelector } from "react-redux";

import api from "@/services/api";

export const OCR_ENGINES = ["tesseract", "kraken"];
export const MAX_TESSERACT_MODELS = 7;

const EMPTY = [];

const firstModelIdOf = (profiles, engine) => {
	const first = profiles.find(p => p.engine === engine);
	return first ? [first.id] : [];
};

const useOCRModelSelection = (transcriptionId) => {
	const OCRProfiles = useSelector(state => state.app.OCRProfiles) || EMPTY;

	// only offer engines that actually have models
	const availableEngines = useMemo(
		() => OCR_ENGINES.filter(engine => OCRProfiles.some(p => p.engine === engine)),
		[OCRProfiles],
	);
	const initialEngine = availableEngines[0] || OCR_ENGINES[0];

	const [selectedEngine, setSelectedEngine] = useState(initialEngine);
	const [selectedModels, setSelectedModels] = useState(() => firstModelIdOf(OCRProfiles, initialEngine));

	const engineModels = useMemo(
		() => OCRProfiles.filter(p => p.engine === selectedEngine),
		[OCRProfiles, selectedEngine],
	);

	const isMultiSelect = selectedEngine === "tesseract";
	const canAddModel = isMultiSelect
		&& selectedModels.length < engineModels.length
		&& selectedModels.length < MAX_TESSERACT_MODELS;
	const canRemoveModel = selectedModels.length > 1;

	const changeEngine = (engine) => {
		setSelectedEngine(engine);
		setSelectedModels(firstModelIdOf(OCRProfiles, engine));
	};

	const setSelectedModel = (index, modelId) => {
		setSelectedModels(prev => {
			const models = prev.slice();
			models[index] = Number(modelId);
			return models;
		});
	};

	const addModel = () => {
		setSelectedModels(prev => {
			const next = engineModels.find(p => !prev.includes(p.id));
			return next ? [...prev, next.id] : prev;
		});
	};

	const removeModel = () => {
		setSelectedModels(prev => (prev.length > 1 ? prev.slice(0, -1) : prev));
	};

	useEffect(() => {
		api.get(`transcriptions/${transcriptionId}/default-model`).then(({ data }) => {
			if (!data) return;
			// expects model ids joined with '+', e.g. "99" or "12+15"
			const models = String(data).split('+').map(Number)
				.map(id => OCRProfiles.find(p => p.id === id))
				.filter(Boolean);
			if (!models.length) return;

			const engine = models[0].engine;
			const sameEngine = models.filter(m => m.engine === engine).map(m => m.id);

			setSelectedEngine(engine);
			setSelectedModels(engine === "tesseract" ? sameEngine : [sameEngine[0]]);
		}).catch(() => { });
	}, [transcriptionId]);

	return {
		availableEngines,
		selectedEngine,
		changeEngine,
		engineModels,
		selectedModels,
		setSelectedModel,
		addModel,
		removeModel,
		isMultiSelect,
		canAddModel,
		canRemoveModel,
		// '+'-joined and URL-encoded (a raw '+' in a query string is decoded as a space)
		encodedModels: encodeURIComponent(selectedModels.join('+')),
	};
};

export default useOCRModelSelection;