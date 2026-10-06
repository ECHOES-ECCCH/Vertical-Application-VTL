import viewerInitialState from "@/store/initialState/viewer.initialState";

export function viewer(state = viewerInitialState, action) {
	switch (action.type) {
		case "VIEWER_ISLOADED":
			return {
				...state,
				isLoaded: action.value,
			};

		case "VIEWER_ISMOBILE":
			return {
				...state,
				isMobile: action.value,
			};

		case "VIEWER_ISPROCESSING":
			return {
				...state,
				isProcessing: action.value,
			};

		case "VIEWER_NOACCESS":
			return {
				...state,
				noAccess: action.value,
			};

		case "VIEWER_SHOW_TEXT_STRUCTURE":
			return {
				...state,
				showTextStructure: action.value,
			};

		case "VIEWER_ISSAVING":
			return {
				...state,
				isSaving: action.value,
			};

		case "VIEWER_RECEIVE_DATA":
			return {
				...state,
				data: {
					...state.data,
					...action.data,
				},
			};

		case "VIEWER_RECEIVE_INFOJSON":
			return {
				...state,
				ocd: {
					...state.ocd,
					tileSources: {
						...action.data,
					},
				},
			};

		case "VIEWER_RECEIVE_VERSE_FORMATTING":
			return {
				...state,
				data: {
					...state.data,
					verses: state.data.verses.map(verse =>
						verse.id === action.verseId ? { ...verse, image: false, formatting: action.data } : verse,
					),
				},
			};

		case "VIEWER_RESET_VERSES_ACTIVE":
			return {
				...state,
				editor: {
					...state.editor,
					verseActive: null,
					wordActive: {
						verseId: null,
						wordId: null,
					},
					verseGroupActive: {
						firstVerseNo: null,
						lastVerseNo: null,
						activeVersesIds: [],
					},
					wordGroupActive: {
						verseId: null,
						firstWordNo: null,
						lastWordNo: null,
						activeWordsIds: [],
					},
				},
			};

		case "VIEWER_SET_VERSE_ACTIVE":
			return {
				...state,
				editor: {
					...state.editor,
					verseActive: action.verseId,
					wordActive: {
						verseId: null,
						wordId: null,
					},
					verseGroupActive: {
						firstVerseNo: null,
						lastVerseNo: null,
						activeVersesIds: [],
					},
					wordGroupActive: {
						verseId: null,
						firstWordNo: null,
						lastWordNo: null,
						activeWordsIds: [],
					},
				},
			};

		case "VIEWER_SET_VERSES_ACTIVE_RANGE": {
			let finalFirstVerseNo;
			let finalLastVerseNo;
			let numberOfVersesInLayer = 0;
			for (let i  = 0; i < state.data.verses.length; ++i) {
				if (state.data.verses[i].pageLayer.layerId == state.selectedLayer) {
					numberOfVersesInLayer++;
				}
			}
			if (action.firstVerseId !== undefined) {
				for (let i  = 0; i < state.data.verses.length; ++i) {
					if (state.data.verses[i].pageLayer.layerId == state.selectedLayer) {
						if (state.data.verses[i].id == action.firstVerseId) {
							finalFirstVerseNo = state.data.verses[i].verseNo;
						}
					}
				}
			} else if (action.firstVerseNo !== undefined) {
				finalFirstVerseNo = action.firstVerseNo;
			}
			if (action.lastVerseId !== undefined) {
				for (let i  = 0; i < state.data.verses.length; ++i) {
					if (state.data.verses[i].id == action.lastVerseId &&
						state.data.verses[i].pageLayer.layerId == state.selectedLayer) {
						finalLastVerseNo = state.data.verses[i].verseNo;
						break;
					}
				}
			} else if (action.lastVerseNo !== undefined) {
				finalLastVerseNo = action.lastVerseNo;
			}

			if (action.firstOffset) {
				if (finalFirstVerseNo + action.firstOffset < 1) {
					finalFirstVerseNo = 1;
				} else if(finalFirstVerseNo + action.firstOffset > numberOfVersesInLayer) {
					finalFirstVerseNo = numberOfVersesInLayer;
				} else {
					finalFirstVerseNo = finalFirstVerseNo + action.firstOffset;
				}
			}

			if(action.lastOffset) {
				if (finalLastVerseNo + action.lastOffset < 1) {
					finalLastVerseNo = 1;
				} else if(finalLastVerseNo + action.lastOffset > numberOfVersesInLayer) {
					finalLastVerseNo = numberOfVersesInLayer;
				} else {
					finalLastVerseNo = finalLastVerseNo + action.lastOffset;
				}
			}

			if(!state.data.verses.find((val) => { return val.verseNo == finalFirstVerseNo && val.pageLayer.layerId == state.selectedLayer; })) {
				finalFirstVerseNo = undefined;
			}

			if(!state.data.verses.find((val) => { return val.verseNo == finalLastVerseNo && val.pageLayer.layerId == state.selectedLayer; })) {
				finalLastVerseNo = undefined;
			}

			if (finalFirstVerseNo !== undefined && finalLastVerseNo !== undefined && finalFirstVerseNo == finalLastVerseNo) {
				let finalVerse = state.data.verses.find((val) => { return val.verseNo == finalFirstVerseNo && val.pageLayer.layerId == state.selectedLayer; });

				if (finalVerse) {
					return {
						...state,
						editor: {
							...state.editor,
							verseActive: finalVerse.id,
							wordActive: {
								verseId: null,
								wordId: null,
							},
							verseGroupActive: {
								firstVerseNo: null,
								lastVerseNo: null,
								activeVersesIds: [],
							},
							wordGroupActive: {
								verseId: null,
								firstWordNo: null,
								lastWordNo: null,
								activeWordsIds: [],
							},
						},
					};
				} else {
					return state;
				}
			} else if (finalFirstVerseNo !== undefined && finalLastVerseNo !== undefined) {
				let firstNum = finalFirstVerseNo > finalLastVerseNo ? finalLastVerseNo : finalFirstVerseNo;
				let lastNum = finalFirstVerseNo < finalLastVerseNo ? finalLastVerseNo : finalFirstVerseNo;
				let verseIdArray = [];

				for (let i  = 0; i < state.data.verses.length; ++i) {
					if (state.data.verses[i].verseNo >= firstNum &&
						state.data.verses[i].verseNo <= lastNum &&
						state.data.verses[i].pageLayer.layerId == state.selectedLayer)
					{
						verseIdArray.push(state.data.verses[i].id);
					}
				}
				
				return {
					...state,
					editor: {
						...state.editor,
						verseActive: null,
						wordActive: {
							verseId: null,
							wordId: null,
						},
						verseGroupActive: {
							firstVerseNo: finalFirstVerseNo,
							lastVerseNo: finalLastVerseNo,
							activeVersesIds: verseIdArray,
						},
						wordGroupActive: {
							verseId: null,
							firstWordNo: null,
							lastWordNo: null,
							activeWordsIds: [],
						},
					},
				};
			} else {
				return state;
			}
		}

		case "VIEWER_SET_VERSES_ACTIVE": {
			if (state.editor.verseActive == null && state.editor.verseGroupActive.firstVerseNo == null) {
				return {
					...state,
					editor: {
						...state.editor,
						verseActive: action.verseId,
						wordActive: {
							verseId: null,
							wordId: null,
						},
						verseGroupActive: {
							firstVerseNo: null,
							lastVerseNo: null,
							activeVersesIds: [],
						},
						wordGroupActive: {
							verseId: null,
							firstWordNo: null,
							lastWordNo: null,
							activeWordsIds: [],
						},
					},
				};
			}
			
			if (state.editor.verseActive != null || state.editor.verseGroupActive.firstVerseNo != null) {
				let currentVerseNum = null;
				let newVerseNum = null;

				if (state.editor.verseGroupActive.firstVerseNo != null) {
					currentVerseNum = state.editor.verseGroupActive.firstVerseNo;

					for (let i  = 0; i < state.data.verses.length; ++i) {
						if (state.data.verses[i].id == action.verseId) {
							newVerseNum = state.data.verses[i].verseNo;
						}
					}
				} else {
					for (let i  = 0; i < state.data.verses.length; ++i) {
						if (state.data.verses[i].id == state.editor.verseActive) {
							currentVerseNum = state.data.verses[i].verseNo;
						}

						if (state.data.verses[i].id == action.verseId) {
							newVerseNum = state.data.verses[i].verseNo;
						}
					}
				}

				if (currentVerseNum == null || newVerseNum == null) {
					return state;
				}

				let firstNum = currentVerseNum > newVerseNum ? newVerseNum : currentVerseNum;
				let lastNum = currentVerseNum < newVerseNum ? newVerseNum : currentVerseNum;
				let verseIdArray = [];

				for (let i  = 0; i < state.data.verses.length; ++i) {
					if (state.data.verses[i].verseNo >= firstNum &&
						state.data.verses[i].verseNo <= lastNum &&
						state.data.verses[i].pageLayer.layerId == state.selectedLayer)
					{
						verseIdArray.push(state.data.verses[i].id);
					}
				}

				return {
					...state,
					editor: {
						...state.editor,
						verseActive: null,
						wordActive: {
							verseId: null,
							wordId: null,
						},
						verseGroupActive: {
							firstVerseNo: currentVerseNum,
							lastVerseNo: newVerseNum,
							activeVersesIds: verseIdArray,
						},
						wordGroupActive: {
							verseId: null,
							firstWordNo: null,
							lastWordNo: null,
							activeWordsIds: [],
						},
					},
				};
			}

			if (state.editor.verseGroupActive.firstVerseNo != null) {
				let currentVerseNum = state.editor.verseGroupActive.firstVerseNo;
				let newVerseNum = null;

				for (let i  = 0; i < state.data.verses.length; ++i) {
					if (state.data.verses[i].id == action.verseId) {
						newVerseNum = state.data.verses[i].verseNo;
					}
				}

				if (currentVerseNum == null || newVerseNum == null) {
					return state;
				}

				let firstNum = currentVerseNum > newVerseNum ? newVerseNum : currentVerseNum;
				let lastNum = currentVerseNum < newVerseNum ? newVerseNum : currentVerseNum;
				let verseIdArray = [];

				for (let i  = 0; i < state.data.verses.length; ++i) {
					if (state.data.verses[i].verseNo >= firstNum &&
						state.data.verses[i].verseNo <= lastNum &&
						state.data.verses[i].pageLayer.layerId == state.selectedLayer)
					{
						verseIdArray.push(state.data.verses[i].id);
					}
				}

				return {
					...state,
					editor: {
						...state.editor,
						verseActive: null,
						wordActive: {
							verseId: null,
							wordId: null,
						},
						verseGroupActive: {
							firstVerseNo: currentVerseNum,
							lastVerseNo: newVerseNum,
							activeVersesIds: verseIdArray,
						},
						wordGroupActive: {
							verseId: null,
							firstWordNo: null,
							lastWordNo: null,
							activeWordsIds: [],
						},
					},
				};
			}

			return {
				...state,
				editor: {
					...state.editor,
					verseActive: action.verseId,
					wordActive: {
						verseId: null,
						wordId: null,
					},
					verseGroupActive: {
						firstVerseNo: null,
						lastVerseNo: null,
						activeVersesIds: [],
					},
					wordGroupActive: {
						verseId: null,
						firstWordNo: null,
						lastWordNo: null,
						activeWordsIds: [],
					},
				},
			};
		}

		case "VIEWER_SET_VERSE_EDIT":
			return {
				...state,
				editor: {
					...state.editor,
					verseEdit: action.data,
				},
			};

		case "VIEWER_SET_WORD_ACTIVE":
			return {
				...state,
				editor: {
					...state.editor,
					verseActive: null,
					wordActive: {
						verseId: action.verseId,
						wordId: action.wordId,
					},
					verseGroupActive: {
						firstVerseNo: null,
						lastVerseNo: null,
						activeVersesIds: [],
					},
					wordGroupActive: {
						verseId: null,
						firstWordNo: null,
						lastWordNo: null,
						activeWordsIds: [],
					},
				},
			};

		case 'VIEWER_SET_FIRST_WORD_IN_VERSE_ACTIVE': {
			let wordId;
			for (let i  = 0; i < state.data.verses.length; ++i) {
				if (state.data.verses[i].id === action.verseId) {
					if (state.data.verses[i].words.length > 0) {
						wordId = state.data.verses[i].words[0].id;
						break;
					}
				}
			}
			if (wordId) {
				return {
					...state,
					editor: {
						...state.editor,
						verseActive: null,
						wordActive: {
							verseId: action.verseId,
							wordId: wordId,
						},
						verseGroupActive: {
							firstVerseNo: null,
							lastVerseNo: null,
							activeVersesIds: [],
						},
						wordGroupActive: {
							verseId: null,
							firstWordNo: null,
							lastWordNo: null,
							activeWordsIds: [],
						},
					},
				};
			} else {
				return state;
			}
		}

		case "VIEWER_SET_WORDS_ACTIVE":
			if (state.editor.wordActive.wordId == null && state.editor.wordGroupActive.firstWordNo == null || state.editor.wordGroupActive.verseId !== action.verseId && state.editor.wordActive.verseId !== action.verseId) {
				return {
					...state,
					editor: {
						...state.editor,
						verseActive: null,
						wordActive: {
							verseId: action.verseId,
							wordId: action.wordId,
						},
						verseGroupActive: {
							firstVerseNo: null,
							lastVerseNo: null,
							activeVersesIds: [],
						},
						wordGroupActive: {
							verseId: null,
							firstWordNo: null,
							lastWordNo: null,
							activeWordsIds: [],
						},
					},
				};
			}
			
			if (state.editor.wordActive.wordId != null && state.editor.wordActive.verseId === action.verseId || state.editor.wordGroupActive.firstWordNo !== null && state.editor.wordGroupActive.verseId == action.verseId) {
				let currentWordNum = null;
				let newWordNum = null;

				if (state.editor.wordGroupActive.firstWordNo !== null && state.editor.wordGroupActive.verseId == action.verseId) {
					currentWordNum = state.editor.wordGroupActive.firstWordNo;

					for (let i  = 0; i < state.data.verses.length; ++i) {
						if (state.data.verses[i].id == action.verseId) {
							for (let j  = 0; j < state.data.verses[i].words.length; ++j) {
								if (state.data.verses[i].words[j].id == action.wordId) {
									newWordNum = state.data.verses[i].words[j].ordering;
								}
							}
							break;
						}
					}
				} else {
					for (let i  = 0; i < state.data.verses.length; ++i) {
						if (state.data.verses[i].id == action.verseId) {
							for (let j  = 0; j < state.data.verses[i].words.length; ++j) {
								if (state.data.verses[i].words[j].id == state.editor.wordActive.wordId) {
									currentWordNum = state.data.verses[i].words[j].ordering;
								}
								
								if (state.data.verses[i].words[j].id == action.wordId) {
									newWordNum = state.data.verses[i].words[j].ordering;
								}
							}
							break;
						}
					}
				}

				if (currentWordNum == null || newWordNum == null) {
					return state;
				}

				let firstNum = currentWordNum > newWordNum ? newWordNum : currentWordNum;
				let lastNum = currentWordNum < newWordNum ? newWordNum : currentWordNum;
				let wordIdArray = [];

				for (let i  = 0; i < state.data.verses.length; ++i) {
					if (state.data.verses[i].id == action.verseId && state.data.verses[i].pageLayer.layerId == state.selectedLayer) {
						for (let j  = 0; j < state.data.verses[i].words.length; ++j) {
							if (state.data.verses[i].words[j].ordering >= firstNum &&
								state.data.verses[i].words[j].ordering <= lastNum)
							{
								wordIdArray.push(state.data.verses[i].words[j].id);
							}
						}
					}
				}

				return {
					...state,
					editor: {
						...state.editor,
						verseActive: null,
						wordActive: {
							verseId: null,
							wordId: null,
						},
						verseGroupActive: {
							firstVerseNo: null,
							lastVerseNo: null,
							activeVersesIds: [],
						},
						wordGroupActive: {
							verseId: action.verseId,
							firstWordNo: currentWordNum,
							lastWordNo: newWordNum,
							activeWordsIds: wordIdArray,
						},
					},
				};
			}

			return {
				...state,
				editor: {
					...state.editor,
					verseActive: null,
					wordActive: {
						verseId: action.verseId,
						wordId: action.wordId,
					},
					verseGroupActive: {
						firstVerseNo: null,
						lastVerseNo: null,
						activeVersesIds: [],
					},
					wordGroupActive: {
						verseId: null,
						firstWordNo: null,
						lastWordNo: null,
						activeWordsIds: [],
					},
				},
			};

		case "VIEWER_SET_WORDS_ACTIVE_RANGE": {
			let tempVerse;

			for (let i = 0; i < state.data.verses.length; ++i) {
				if (state.data.verses[i].id == action.verseId &&
					state.data.verses[i].pageLayer.layerId == state.selectedLayer) {
					tempVerse = state.data.verses[i];
				}
			}

			if (!tempVerse) { return state; }

			let finalFirstWordId;
			let finalLastWordId;
			let finalFirstWordNo;
			let finalLastWordNo;
			let finalFirstWordIndex;
			let finalLastWordIndex;

			if (action.firstWordId != undefined) {
				for (let i = 0; i < tempVerse.words.length; ++i) {
					if (tempVerse.words[i].id == action.firstWordId) {
						finalFirstWordNo = tempVerse.words[i].ordering;
						finalFirstWordId = tempVerse.words[i].id;
						finalFirstWordIndex = i;
						break;
					}
				}
			} else if (action.firstWordNo != undefined) {
				finalFirstWordNo = action.firstWordNo;
				for (let i = 0; i < tempVerse.words.length; ++i) {
					if (tempVerse.words[i].ordering == action.firstWordNo) {
						finalFirstWordId = tempVerse.words[i].id;
						finalFirstWordIndex = i;
						break;
					}
				}	
			}
			if (action.lastWordId != undefined) {
				for (let i = 0; i < tempVerse.words.length; ++i) {
					if (tempVerse.words[i].id == action.firstWordId) {
						finalLastWordNo = tempVerse.words[i].ordering;
						finalLastWordId = tempVerse.words[i].id;
						finalLastWordIndex = i;
						break;
					}
				}	
			} else if (action.lastWordNo != undefined) {
				finalLastWordNo = action.lastWordNo;
				for (let i = 0; i < tempVerse.words.length; ++i) {
					if (tempVerse.words[i].ordering == action.lastWordNo) {
						finalLastWordId = tempVerse.words[i].id;
						finalLastWordIndex = i;
						break;
					}
				}	
			}

			if (action.firstOffset) {
				let finalIndex;
				if (finalFirstWordIndex + action.firstOffset < 0) {
					finalIndex = 0;
				} else if(finalFirstWordIndex + action.firstOffset > tempVerse.words.length - 1) {
					finalIndex = tempVerse.words.length - 1;
				} else {
					finalIndex = finalFirstWordIndex + action.firstOffset;
				}
				finalFirstWordNo = tempVerse.words[finalIndex]?.ordering;
				finalFirstWordId = tempVerse.words[finalIndex]?.id;
			}

			if(action.lastOffset) {
				let finalIndex;
				if (finalLastWordIndex + action.lastOffset < 0) {
					finalIndex = 0;
				} else if(finalLastWordIndex + action.lastOffset > tempVerse.words.length - 1) {
					finalIndex = tempVerse.words.length - 1;
				} else {
					finalIndex = finalLastWordIndex + action.lastOffset;
				}
				finalLastWordNo = tempVerse.words[finalIndex]?.ordering;
				finalLastWordId = tempVerse.words[finalIndex]?.id;
			}

			if(!tempVerse.words.find((val) => { return val.ordering == finalFirstWordNo; })) {
				finalFirstWordNo = undefined;
			}

			if(!tempVerse.words.find((val) => { return val.ordering == finalLastWordNo; })) {
				finalLastWordNo = undefined;
			}

			if (finalFirstWordNo !== undefined && finalLastWordNo !== undefined && finalFirstWordNo == finalLastWordNo) {
				return {
					...state,
					editor: {
						...state.editor,
						verseActive: null,
						wordActive: {
							verseId: action.verseId,
							wordId: finalFirstWordId,
						},
						verseGroupActive: {
							firstVerseNo: null,
							lastVerseNo: null,
							activeVersesIds: [],
						},
						wordGroupActive: {
							verseId: null,
							firstWordNo: null,
							lastWordNo: null,
							activeWordsIds: [],
						},
					},
				};
			} else if (finalFirstWordNo !== undefined && finalLastWordNo !== undefined) {
				let firstNum = finalFirstWordNo > finalLastWordNo ? finalLastWordNo : finalFirstWordNo;
				let lastNum = finalFirstWordNo < finalLastWordNo ? finalLastWordNo : finalFirstWordNo;
				let wordIdArray = [];

				for (let i  = 0; i < tempVerse.words.length; ++i) {
					if (tempVerse.words[i].ordering >= firstNum &&
						tempVerse.words[i].ordering <= lastNum)
					{
						wordIdArray.push(tempVerse.words[i].id);
					}
				}
				
				return {
					...state,
					editor: {
						...state.editor,
						verseActive: null,
						wordActive: {
							verseId: null,
							wordId: null,
						},
						verseGroupActive: {
							firstVerseNo: null,
							lastVerseNo: null,
							activeVersesIds: [],
						},
						wordGroupActive: {
							verseId: action.verseId,
							firstWordNo: finalFirstWordNo,
							lastWordNo: finalLastWordNo,
							activeWordsIds: wordIdArray,
						},
					},
				};
			} else {
				return state;
			}
		}

		case "VIEWER_SET_WORD_EDIT":
			return {
				...state,
				editor: {
					...state.editor,
					wordEdit: action.data,
				},
			};

		case "VIEWER_SET_SELECTION_EDIT":
			return {
				...state,
				ocd: {
					...state.ocd,
					selection: action.data,
				},
			};

		case "VIEWER_SET_STYLE_ACTIVE":
			return {
				...state,
				editor: {
					...state.editor,
					styleActive: action.data,
				},
			};

		case "VIEWER_SET_STYLE_CURRENT":
			return {
				...state,
				editor: {
					...state.editor,
					styleCurrent: action.data,
				},
			};

		case "VIEWER_SET_INSERT_CHARACTER":
			return {
				...state,
				editor: {
					...state.editor,
					addChar: action.data,
				},
			};

		case "VIEWER_SET_VERSE_CONTENT":
			return {
				...state,
				data: {
					...state.data,
					verses: state.data.verses.map(verse =>
						verse.id === action.verseId ? { ...verse, content: action.verseContent } : verse,
					),
				},
			};

		case "VIEWER_SET_VERSE_BOUNDS":
			return {
				...state,
				data: {
					...state.data,
					verses: state.data.verses.map(verse =>
						verse.id === action.verseId ? { ...verse, boundingBox: action.bounds } : verse,
					),
				},
			};
		
		case "VIEWER_SET_WORD_BOUNDS":
			return {
				...state,
				data: {
					...state.data,
					verses: state.data.verses.map(verse =>
						verse.id === action.verseId ? 
							{ 
								...verse, 
								words: verse.words.map(word => 
									word.id === action.wordId ? { ...word, boundingBox: action.bounds } : word,
								),
							} : 
							verse,
					),
				},
			};

		case "VIEWER_SET_VERSE_STUDENT_CONTENT":
			return {
				...state,
				data: {
					...state.data,
					studentVerses: state.data.studentVerses.map(verse =>
						verse.id === action.verseId ? { ...verse, content: action.verseContent } : verse,
					),
				},
			};

		case "VIEWER_SET_VERSE_STUDENT_STATUS":
			return {
				...state,
				data: {
					...state.data,
					studentVerses: state.data.studentVerses.map(verse =>
						verse.id === action.verseId ? { ...verse, learningStatus: action.verseStudentStatus } : verse,
					),
				},
			};

		case "VIEWER_UPDATE_VERSE_COMMENTS_COUNT": {
			const iterateOverBlocks = (block) => {
				return block.map((element) => element.blockId ? { ...element, children: iterateOverBlocks(element.children) } : (element.id === action.verseId  ? { ...element, comments: action.verseCommentsCount } : element));
			};

			return {
				...state,
				transcriptionLayersData: {
					...state.transcriptionLayersData,
					layers: state.transcriptionLayersData.layers.map(pageLayer => 
						pageLayer.verses.includes(action.verseId) ? { ...pageLayer, blocks: iterateOverBlocks(pageLayer.blocks) } : pageLayer,
					),
				},
				data: {
					...state.data,
					pageLayers: state.data.pageLayers.map(pageLayer => 
						pageLayer.verses.includes(action.verseId) ? { ...pageLayer, blocks: iterateOverBlocks(pageLayer.blocks) } : pageLayer,
					),
					verses: state.data.verses.map(verse =>
						verse.id === action.verseId ? { ...verse, comments: action.verseCommentsCount } : verse,
					),
				},
			};
		}
		case "VIEWER_UPDATE_REMOVED_INDEX":
			return {
				...state,
				data: {
					...state.data,
					verses: state.data.verses.map(verse => action.verseId !== verse.id ? verse : {
						...verse,
						indexes: verse.indexes.filter(index => index.id !== action.indexId),
						words: verse.words.map(word => (action.wordId && action.wordId !== word.id) ? word : {
							...word,
							indexes: word.indexes.filter(index => index.id !== action.indexId),
						}),
					}),
				},
			};
		case "VIEWER_UPDATE_ADDED_INDEX":
			return {
				...state,
				data: {
					...state.data,
					verses: state.data.verses.map(verse => action.verseId !== verse.id ? verse : {
						...verse,
						indexes: verse.indexes.find(index => index.id === action.index.id) ? verse.indexes : [...verse.indexes, action.index],
						words: verse.words.map(word => action.wordId !== word.id ? word : {
							...word,
							indexes: word.indexes.find(index => index.id === action.index.id) ? word.indexes : [...word.indexes, action.index],
						}),
					}),
				},
			};
		case "VIEWER_UPDATE_EDITED_INDEX":
			return {
				...state,
				data: {
					...state.data,
					verses: state.data.verses.map(verse => {
						return {
							...verse,
							indexes: verse.indexes.map(index => action.index.id !== index.id ? index : action.index),
							words: verse.words.map(word => {
								return {
									...word,
									indexes: word.indexes.map(index => action.index.id !== index.id ? index : action.index),
								};
							}),
						};
					}),
				},
			};
		case "VIEWER_SET_VERSE_IMAGE":
			return {
				...state,
				data: {
					...state.data,
					verses: state.data.verses.map(verse =>
						verse.id === action.verseId ? { ...verse, image: true } : verse,
					),
				},
			};

		case "VIEWER_RELOAD_VERSES":
			return {
				...state,
				data: {
					...state.data,
					verses: action.verses,
				},
			};

		case "VIEWER_RELOAD_BLOCKS":
			return {
				...state,
				data: {
					...state.data,
					pageLayers: state.data.pageLayers.map((layer) => 
						layer.layer.id == action.layerId ? { ...layer, blocks: action.newBlocks } : layer,
					),
				},
				transcriptionLayersData: {
					...state.transcriptionLayersData,
					layers: state.transcriptionLayersData.layers.map((layer) => 
						layer.layer.id == action.layerId ? { ...layer, blocks: action.newBlocks } : layer,
					),
				},
			};

		case "VIEWER_SET_VERSE_STATUS":
			return {
				...state,
				data: {
					...state.data,
					verses: state.data.verses.map(verse =>
						verse.id === action.verseId ? { ...verse, status: action.verseStatus } : verse,
					),
				},
			};

		case "VIEWER_SET_CORRECT_TRANSCRIPTION":
			return {
				...state,
				data: {
					...state.data,
					verses: state.data.verses.map(verse =>
						state.data.viewMode === "admin" || state.data.viewMode === "verify" ? {
							...verse,
							status: "verified",
						} : { ...verse, status: verse.status !== "verified" ? "transcribed" : verse.status },
					),
				},
			};

		case "CLEAR_TRANSCRIPTION_DATA":
			return {
				...state, data: {
					id: null,
					path: "",
					pageNo: null,
					boundingBox: {},
					verses: [],
					pageLayers: [],
					studentVerses: [],
					titleLabel: "",
					prevPageId: null,
					nextPageId: null,
					viewMode: "",
				},
			};

		case "VIEWER_CHANGE_WORKING_PAGE_LAYER":
			return {
				...state,
				workingPageLayer: action.workingPageLayer,
			};

		case "VIEWER_SET_LAYER_VISIBILITY":
			return {
				...state,
				transcriptionLayersData: {
					...state.transcriptionLayersData,
					layers: state.transcriptionLayersData.layers.map(layerToEdit => {
						if (layerToEdit.layer.id === action.layerId) {
							return { 
								...layerToEdit, 
								isShown: action.value, 
							};	
						}
						return layerToEdit;
					}),
				},
				editor: {
					...state.editor,
				},
			};

		case "VIEWER_SET_SINGLE_VISIBLE_LAYER":
			return {
				...state,
				transcriptionLayersData: {
					...state.transcriptionLayersData,
					layers: state.transcriptionLayersData.layers.map(layerToEdit => {
						if (layerToEdit.layer.id === action.layerId) {
							return { 
								...layerToEdit, 
								isShown: true, 
							};	
						} else {
							return { 
								...layerToEdit, 
								isShown: false, 
							};	
						}
					}),
				},
				editor: {
					...state.editor,
					verseActive: null,
				},
			};

		case "VIEWER_RELOAD_VERSES_AFTER_CHANGING_VISIBILITY":
			return {
				...state,
				data: {
					...state.data,
					verses: action.verses,
				},
			};

		case "VIEWER_SET_SEARCH_RESULTS":
			return {
				...state,
				searchResults: {
					searchedPhrase: action.phraseSearchedFor,
					totalHits: action.results.totalHits ? action.results.totalHits : 0,
					pages: action.results.pages ? action.results.pages : new Map(),
				},
			};

		case "VIEWER_SET_TRANSCRIPTION_PAGES_DATA": 
			return {
				...state,
				transcriptionPagesData: action.transcriptionPagesData,
			};

		case "VIEWER_SET_TRANSCRIPTION_LAYERS_DATA": {
			return {
				...state,
				transcriptionLayersData: action.transcriptionLayersData,
				annotationLayer: (
					() => { 
						for (let i = 0; i < action.transcriptionLayersData.layers.length; ++i) {
							if (action.transcriptionLayersData.layers[i].layer.id === state.selectedLayer) {
								if (action.transcriptionLayersData.layers[i].layer.type === "IIIF_ANNOTATION") {
									return true;
								} else {
									return false;
								}
							}
						}
						return false;
					}
				)(),
			};
		}

		case "VIEWER_SET_SELECTED_TRANSCRIPTION_LAYER": {
			return {
				...state,
				selectedLayer: action.selectedLayer,
				annotationLayer: (
					() => { 
						for (let i = 0; i < state.transcriptionLayersData.layers.length; ++i) {
							if (state.transcriptionLayersData.layers[i].layer.id === action.selectedLayer) {
								if (state.transcriptionLayersData.layers[i].layer.type === "IIIF_ANNOTATION") {
									return true;
								} else {
									return false;
								}
							}
						}
						return false;
					}
				)(),
				editor: {
					...state.editor,
					verseActive: null,
				},
			};
		}

		case "VIEWER_DISABLE_UNDO_AND_REDO":
			return {
				...state,
				editor: {
					...state.editor,
					redoPossible: false,
					undoPossible: false,
				},
			};

		case "VIEWER_UPDATE_UNDO_AND_REDO":
			return {
				...state,
				editor: {
					...state.editor,
					redoPossible: action.canRedo,
					undoPossible: action.canUndo,
				},
			};

		case "VIEWER_ALLOW_UNDO_DISALLOW_REDO":
			return {
				...state,
				editor: {
					...state.editor,
					redoPossible: false,
					undoPossible: true,
				},
			};

		case "VIEWER_SET_SHIFT_PRESSED":
			return {
				...state,
				editor: {
					...state.editor,
					shiftPressed: action.data,
				},
			};

		case "VIEWER_SET_TEI_PB_VALUE":
			return {
				...state,
				data: {
					...state.data,
					teiPbValue: action.data,
				},
			};

		case "VIEWER_SET_PAGE_EMPTY":
			return {
				...state,
				data: {
					...state.data,
					empty: action.data,
				},
			};
		
		default:
			return state;
	}
}
