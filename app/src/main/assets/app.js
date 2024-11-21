// Load the TensorFlow.js model (MobileNet)
async function loadModel() {
    const model = await mobilenet.load();
    console.log("Model Loaded");
    return model;
}

// Analyze images using TensorFlow.js
async function analyzeImages() {
    const imageInput = document.getElementById('imageInput');
    const files = imageInput.files;

    if (!files.length) {
        alert("Please upload an image first.");
        return;
    }

    const model = await loadModel();
    let results = [];
    for (let file of files) {
        const img = document.createElement("img");
        img.src = URL.createObjectURL(file);

        await new Promise(resolve => img.onload = resolve);

        // Perform prediction
        const predictions = await model.classify(img);
        console.log("Predictions:", predictions);

         // Display results
         const li = document.createElement("li");
         li.textContent = `Predictions for ${file.name}: ${predictions.map(p => p.className).join(", ")}`;
         predictionList.appendChild(li);

        // Extract results
        const title = predictions[0]?.className || "General Request";
        const description = predictions.map(p => p.className).join(", ");
        const serviceType = determineServiceType(predictions);

        results.push({ title, serviceType, description });
    }

    // Pass results back to Android via JavascriptInterface
    // Android.onAIResult(JSON.stringify(results));
}

// Map predictions to service types
function determineServiceType(predictions) {
    const SERVICE_TYPE_MAPPING = {
        PLUMBER: ["Pipe", "Leak", "Plumbing"],
        ELECTRICIAN: ["Electric", "Wiring", "Light"],
        TUTOR: ["Book", "Notebook", "Desk"],
        EVENTPLANNER: ["Table", "Chair", "Decoration"],
        WRITER: ["Keyboard", "Pen", "Paper"],
        CLEANER: ["Broom", "Bucket", "Cleaning"],
        CARPENTER: ["Wood", "Hammer", "Furniture"],
        PHOTOGRAPHER: ["Camera", "Lens", "Tripod"],
        PERSONAL_TRAINER: ["Dumbbell", "Weight", "Fitness"],
        HAIR_STYLIST: ["Scissors", "Hair", "Brush"],
        OTHER: []
    };

    for (const [type, keywords] of Object.entries(SERVICE_TYPE_MAPPING)) {
        if (predictions.some(p => keywords.includes(p.className))) {
            return type;
        }
    }
    return "OTHER";
}
