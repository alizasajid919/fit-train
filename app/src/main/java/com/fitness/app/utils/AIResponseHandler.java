package com.fitness.app.utils;

import com.fitness.app.models.ChatMessage;
import com.fitness.app.models.User;

import java.util.List;
import java.util.Locale;

public class AIResponseHandler {

    public static String generateResponse(String query, User user) {
        return generateResponse(query, user, null);
    }

    public static String generateResponse(String query, User user, List<ChatMessage> history) {
        String lower = query.toLowerCase(Locale.getDefault());

        String name = "Athlete";
        String goal = "Improve Fitness";
        double weight = 70.0;
        double height = 170.0;
        double targetWeight = 0.0;

        if (user != null) {
            if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
                name = user.getFirstName();
            }
            if (user.getGoal() != null && !user.getGoal().trim().isEmpty()) {
                goal = user.getGoal();
            }
            if (user.getWeight() > 0) weight = user.getWeight();
            if (user.getHeight() > 0) height = user.getHeight();
            if (user.getTargetWeight() > 0) targetWeight = user.getTargetWeight();
        }

        double heightM = height / 100.0;
        double bmi = weight / (heightM * heightM);

        // Analyze conversation history to track the active topic (True Context Memory)
        String activeTopic = "";
        if (history != null && !history.isEmpty()) {
            for (int i = history.size() - 1; i >= 0; i--) {
                ChatMessage m = history.get(i);
                if ("USER".equals(m.getSender())) {
                    String text = m.getText().toLowerCase(Locale.getDefault());
                    if (text.contains("physics") || text.contains("quantum") || text.contains("mechanics") || text.contains("relativity") || text.contains("gravity")) {
                        activeTopic = "physics";
                        break;
                    } else if (text.contains("chemistry") || text.contains("chemical") || text.contains("organic")) {
                        activeTopic = "chemistry";
                        break;
                    } else if (text.contains("biology") || text.contains("cell") || text.contains("evolution") || text.contains("genetic")) {
                        activeTopic = "biology";
                        break;
                    } else if (text.contains("science")) {
                        activeTopic = "science";
                        break;
                    } else if (text.contains("math") || text.contains("algebra") || text.contains("calculus")) {
                        activeTopic = "mathematics";
                        break;
                    } else if (text.contains("diet") || text.contains("eat") || text.contains("food") || text.contains("nutrition")) {
                        activeTopic = "diet";
                        break;
                    } else if (text.contains("workout") || text.contains("exercise") || text.contains("gym")) {
                        activeTopic = "workout";
                        break;
                    } else if (text.contains("computer science") || text.contains("programming") || text.contains("code") || text.contains("java") || text.contains("python")) {
                        activeTopic = "programming";
                        break;
                    } else if (text.contains("history")) {
                        activeTopic = "history";
                        break;
                    } else if (text.contains("geography")) {
                        activeTopic = "geography";
                        break;
                    }
                }
            }
        }

        // Determine if input is Roman Urdu/Urdu
        boolean isRomanUrdu = lower.contains("kya") || lower.contains("hai") || lower.contains("tum") || lower.contains("mujhe")
                || lower.contains("he") || lower.contains("ho") || lower.contains("vazn") || lower.contains("khana")
                || lower.contains("kaise") || lower.contains("karna") || lower.contains("aik") || lower.contains("aur")
                || lower.contains("batao") || lower.contains("shukriya") || lower.contains("theek");

        // 1. Identity & Robot & Human check
        if (lower.contains("robot") || lower.contains("bot") || lower.contains("tum kaun ho") 
                || lower.contains("who are you") || lower.contains("aik machine") 
                || lower.contains("insan") || lower.contains("insaan") || lower.contains("human")) {
            if (isRomanUrdu) {
                return "Nahi, main koi insan ya simple machine nahi hoon! Main aapka AI Fitness Coach aur personalized digital assistant hoon. Main aapke workouts, healthy meals aur queries ko resolve karne ke liye design kiya gaya hoon.";
            } else {
                return "No, I am not a human. I am your AI Fitness Coach and intelligent virtual assistant! I'm here to help guide your fitness training, design meal plans, and answer any general queries you might have.";
            }
        }

        // 1.5 Fitness App Definition check
        if (lower.contains("fitness app")) {
            if (isRomanUrdu) {
                return "Fitness app ek mobile software hota hai jo aapko physical activities, workout routines, steps count, calorie intake, aur diet plans track karne mein madad karta hai, taa ke aap apne health goals achieve kar sakein (bilkul FitTrain ki tarah!).";
            } else {
                return "A fitness app is a mobile application designed to assist users in tracking their physical activities, workouts, water intake, daily steps, and nutritional goals to maintain a healthy lifestyle—just like FitTrain!";
            }
        }

        // 2. Greetings
        if (lower.contains("hello") || lower.contains("hi ") || lower.contains("hey") || lower.contains("assalam") || lower.contains("aoa")) {
            if (isRomanUrdu) {
                return String.format("Assalam-o-Alaikum %s! Main aapka AI Coach hoon. Aapka target hai to %s. Kaise help karoon main aapki aaj?", name, goal.toLowerCase());
            } else {
                return String.format("Hello %s! I'm your AI Coach. I see your target is to %s, and your current weight is %.1f kg (BMI: %.1f). How can I help guide your workouts or diet plan today?", name, goal.toLowerCase(), weight, bmi);
            }
        }

        // 3. How are you
        if (lower.contains("how are you") || lower.contains("how do you do") || lower.contains("kaise ho") || lower.contains("kya haal")) {
            if (isRomanUrdu) {
                return "Main bilkul theek hoon! Shukriya poochne ke liye. Aap sunayein, aapki fitness and health routines kaisi chal rahi hain?";
            } else {
                return "I'm doing great, thank you for asking! How are your health goals and workout routines going today?";
            }
        }

        // 4. Physics Specific Queries (Quantum, Classical Mechanics, Thermodynamics, Electromagnetism, Astrophysics, Relativity, Gravity)
        if (lower.contains("quantum")) {
            if (isRomanUrdu) {
                return "Quantum Mechanics physics ki wo branch hai jo atom aur sub-atomic level par particles ke behavior ko study karti hai. Isme wave-particle duality, superposition, aur quantum entanglement jaise phenomena parhe jaate hain.";
            } else {
                return "Quantum Mechanics is the branch of physics dealing with the behavior of matter and light on the atomic and subatomic scale. It explains phenomena that classical physics cannot, such as wave-particle duality, superposition, and quantum entanglement.";
            }
        }

        if (lower.contains("classical mechanics") || lower.contains("mechanics") && !lower.contains("quantum")) {
            return "Classical Mechanics is the branch of physics that models the motion of macroscopic objects, from projectiles to machinery, spacecraft, and astronomical objects, using Newton's laws of motion.";
        }

        if (lower.contains("thermodynamics")) {
            return "Thermodynamics is the branch of physics that deals with heat, work, and temperature, and their relation to energy, radiation, and physical properties of matter.";
        }

        if (lower.contains("electromagnetism") || lower.contains("electricity") || lower.contains("magnetism")) {
            return "Electromagnetism is the branch of physics studying the electromagnetic force, a type of physical interaction that occurs between electrically charged particles.";
        }

        if (lower.contains("astrophysics") || lower.contains("space physics")) {
            return "Astrophysics is the branch of space science that applies the laws of physics and chemistry to explain the birth, life, and death of stars, planets, galaxies, nebulae, and other objects in the universe.";
        }

        if (lower.contains("relativity")) {
            return "Einstein's Theory of Relativity consists of Special Relativity (laws of physics are same for all observers, speed of light is constant) and General Relativity (gravity is the curvature of spacetime caused by mass and energy).";
        }

        if (lower.contains("gravity") || lower.contains("gravitation")) {
            return "Gravity is the fundamental force by which all things with mass or energy are brought toward one another, giving physical objects weight on Earth.";
        }

        // Branch / Types lists
        boolean asksForTypes = lower.contains("type") || lower.contains("branch") || lower.contains("division") || lower.contains("list them") || lower.contains("tell me");

        if (asksForTypes) {
            if (lower.contains("physics") || activeTopic.equals("physics")) {
                if (isRomanUrdu) {
                    return "Physics ke main types/branches ye hain:\n1. **Classical Mechanics**: Macroscopic objects ki motion.\n2. **Thermodynamics**: Heat aur energy.\n3. **Electromagnetism**: Electric aur magnetic forces.\n4. **Quantum Mechanics**: Subatomic particles ka behavior.\n5. **Relativity**: Space, time, aur gravity ke rules.\n6. **Astrophysics**: Celestial bodies ki physics.";
                } else {
                    return "The primary branches/types of Physics include:\n1. **Classical Mechanics**: Study of the motion of macroscopic objects under forces.\n2. **Thermodynamics**: Study of heat, temperature, and their relation to energy and work.\n3. **Electromagnetism**: Study of electrical charges, magnetic fields, and electromagnetic radiation.\n4. **Quantum Mechanics**: Study of physical phenomena at the microscopic atomic and subatomic scale.\n5. **Relativity**: Einstein's unified framework of space, time, and gravitation.\n6. **Astrophysics**: Application of physics laws to celestial bodies and cosmic events.";
                }
            }
            if (lower.contains("science") || activeTopic.equals("science")) {
                if (isRomanUrdu) {
                    return "Science ke major branches ye hain:\n1. **Natural Sciences**: Physical world ki study (Physics, Chemistry, Biology, Astronomy).\n2. **Social Sciences**: Human behavior aur societies (Psychology, Economics).\n3. **Formal Sciences**: Logic aur Mathematics.";
                } else {
                    return "Science is divided into three major branches:\n1. **Natural Sciences**: Study of the physical and natural world (Physics, Chemistry, Biology, Astronomy).\n2. **Social Sciences**: Study of individuals, human behavior, and societies (Psychology, Sociology, Economics).\n3. **Formal Sciences**: Study of formal systems such as logic, mathematics, and computer science.";
                }
            }
            if (lower.contains("chemistry") || activeTopic.equals("chemistry")) {
                return "The main branches of Chemistry are:\n1. **Organic Chemistry**: Study of carbon-based compounds.\n2. **Inorganic Chemistry**: Study of non-carbon compounds (metals, minerals).\n3. **Physical Chemistry**: Chemical systems using physics principles.\n4. **Analytical Chemistry**: Qualitative and quantitative analysis of substances.\n5. **Biochemistry**: Chemical processes within living organisms.";
            }
            if (lower.contains("biology") || activeTopic.equals("biology")) {
                return "The primary branches of Biology include:\n1. **Zoology**: Study of animals.\n2. **Botany**: Study of plants.\n3. **Microbiology**: Study of microscopic organisms.\n4. **Genetics**: Study of heredity and genes.\n5. **Ecology**: Study of interactions between organisms and their environments.";
            }
            if (lower.contains("math") || activeTopic.equals("mathematics")) {
                return "Mathematics branches include Arithmetic (numbers), Algebra (variables/equations), Geometry (shapes/space), Calculus (change/motion), and Statistics (data/probability).";
            }
        }

        // General definitions
        if (lower.contains("physics")) {
            if (isRomanUrdu) {
                return "Physics (ilm-e-tabiyat) science ki wo branch hai jo matter (madaa), energy (taqat), aur unke aapsi ta'aluq ke laws ko study karti hai. Isme hum mechanics, gravity, light, speed, aur electricity parhte hain.";
            } else {
                return "Physics is the fundamental branch of science concerned with the nature and properties of matter and energy. It covers mechanics, heat, light, radiation, sound, electricity, magnetism, and atomic structures.";
            }
        }

        if (lower.contains("science")) {
            if (isRomanUrdu) {
                return "Science (ilm) observation, experiments, aur systematic study ke zariye hamari kainaat aur uske laws ko samajhne ka naam hai.";
            } else {
                return "Science is the systematic enterprise that builds and organizes knowledge in the form of testable explanations and predictions about the universe, backed by empirical evidence.";
            }
        }

        if (lower.contains("chemistry")) {
            return "Chemistry is the branch of science that studies the composition, structure, properties, and changes of matter. It explores how atoms and molecules interact.";
        }

        if (lower.contains("biology")) {
            return "Biology is the natural science that studies life and living organisms, including their physical structures, chemical processes, physiological mechanisms, and evolution.";
        }

        if (lower.contains("math") || lower.contains("mathematics")) {
            return "Mathematics is the study of numbers, formulas, shapes, and quantities. It forms the logical and analytical foundation for all science, engineering, and technology.";
        }

        // 9. Computer Science, AI, and Technology
        if (lower.contains("computer science") || lower.contains("cs")) {
            return "Computer Science is the study of computation, information, and automation. It spans theoretical disciplines (algorithms, theory of computation) to practical development (software, systems design).";
        }

        if (lower.contains("artificial intelligence") || lower.contains("ai") || lower.contains("machine learning") || lower.contains("deep learning")) {
            return "Artificial Intelligence (AI) is the simulation of human intelligence by machines. It involves training systems to perform tasks like reasoning, learning, computer vision, and natural language processing.";
        }

        if (lower.contains("technology") || lower.contains("tech")) {
            return "Technology is the application of scientific knowledge for practical purposes. It shapes communications, medical treatments, computing, and everyday convenience.";
        }

        // 10. History, Geography
        if (lower.contains("history")) {
            return "History is the systematic study and documentation of past events, civilisations, and human societies. It helps us understand how the past shapes our present.";
        }

        if (lower.contains("geography")) {
            return "Geography is the study of places and the relationships between people and their environments. It covers both physical geography (climates, landforms) and human geography (cultures, populations).";
        }

        // 11. English & Language
        if (lower.contains("english") || lower.contains("grammar") || lower.contains("vocabulary")) {
            return "I can assist you with English grammar rules, vocabulary enhancements, essay structuring, or proofreading. How can I help you improve your writing today?";
        }

        // 12. Business, Career & Education
        if (lower.contains("business") || lower.contains("marketing") || lower.contains("economics")) {
            return "Business involves organized efforts to produce and sell goods/services for profit. Key components include finance, marketing, strategy, and operations.";
        }

        if (lower.contains("career") || lower.contains("job") || lower.contains("interview")) {
            return "For career progression, prioritize building high-value skills, networking, optimizing your resume, and practicing mock interviews. What field are you aiming for?";
        }

        if (lower.contains("education")) {
            return "Education is the acquisition of knowledge, skills, values, and beliefs. It is key to personal development and opening career opportunities.";
        }

        // 13. Travel & Lifestyle
        if (lower.contains("travel") || lower.contains("trip") || lower.contains("suggest place")) {
            return "Traveling is wonderful for mental health and stress relief! For active travel, I recommend checking out nature trails, hiking paths, or cities with rich historic walking routes. What area are you planning to visit?";
        }

        if (lower.contains("productivity") || lower.contains("time management") || lower.contains("focus")) {
            return "To boost productivity, use the Pomodoro technique (work for 25 mins, 5 min active stretch break) and schedule workouts in the morning to elevate focus levels.";
        }

        // 14. Cooking & Recipes
        if (lower.contains("cooking") || lower.contains("recipe") || lower.contains("cook")) {
            return "Cooking is the preparation of food using heat. I can suggest simple, healthy recipes if you let me know what ingredients you have in your kitchen!";
        }

        // 15. Entertainment
        if (lower.contains("entertainment") || lower.contains("movie") || lower.contains("music") || lower.contains("game")) {
            return "Entertainment provides relaxation and fun. It includes movies, music, reading, video games, and sports. Taking rest days with light entertainment is great for active recovery!";
        }

        // 16. Programming Languages (Java, Python, C++, web)
        if (lower.contains("code") || lower.contains("program") || lower.contains("java") || lower.contains("python")
                || lower.contains("programming") || lower.contains("html") || lower.contains("css") || lower.contains("javascript")
                || activeTopic.equals("programming") && (lower.contains("write") || lower.contains("example") || lower.contains("explain"))) {
            if (lower.contains("java")) {
                return "Java is an OOP language. Here is a sample code snippet:\n\n```java\npublic class FitTrain {\n    public static void main(String[] args) {\n        System.out.println(\"Stay active, stay healthy!\");\n    }\n}\n```\nLet me know if you need help with Java concepts!";
            } else if (lower.contains("python")) {
                return "Python is widely used for scripts and AI. Example code snippet:\n\n```python\ndef calculate_water_intake(weight_kg):\n    # Estimate hydration requirement\n    return weight_kg * 35  # ml per day\n```\nWhat Python query do you have today?";
            } else {
                return "I can help you write, debug, and explain code in Java, Python, C++, and web tech. What coding task are we working on? (Remember to stretch regularly during coding!)";
            }
        }

        // 17. Follow-up helpers ("example", "list", "suggest", "more details")
        if (lower.contains("example") || lower.contains("list") || lower.contains("suggest") || lower.contains("more details") || lower.contains("detail")) {
            if (activeTopic.equals("diet")) {
                if (goal.toLowerCase().contains("lose") || goal.toLowerCase().contains("fat")) {
                    return "Here is a detailed Fat Loss meal list:\n- **Breakfast**: 3 scrambled egg whites, 1 slice whole wheat toast, black coffee.\n- **Lunch**: Grilled chicken breast with mixed garden salad (olive oil dressing).\n- **Snack**: A handful of almonds or Greek yogurt.\n- **Dinner**: Baked fish fillet with steamed broccoli and brown rice.";
                } else {
                    return "Here is a Muscle Building high-protein meal list:\n- **Breakfast**: Oatmeal cooked in whole milk, topped with banana and 2 tbsp peanut butter.\n- **Lunch**: Beef stir-fry with rice and spinach.\n- **Snack**: Whey protein shake and handful of mixed nuts.\n- **Dinner**: Salmon steak with sweet potato mash and grilled asparagus.";
                }
            } else if (activeTopic.equals("workout")) {
                if (goal.toLowerCase().contains("lose") || goal.toLowerCase().contains("fat")) {
                    return "Here is a sample HIIT cardio routine (no equipment):\n1. Jumping Jacks (45s work, 15s rest)\n2. Mountain Climbers (45s work, 15s rest)\n3. Burpees (30s work, 30s rest)\n4. Bodyweight Squats (45s work, 15s rest)\n*Repeat for 3-4 circuits!*";
                } else {
                    return "Here is a strength building dumbbell split:\n1. Dumbbell Goblet Squats (3 sets of 10 reps)\n2. Dumbbell Chest Press (3 sets of 12 reps)\n3. Single-arm Dumbbell Rows (3 sets of 12 reps per side)\n4. Dumbbell Shoulder Press (3 sets of 10 reps)";
                }
            }
        }

        // 18. Diet & Nutrition
        if (lower.contains("diet") || lower.contains("eat") || lower.contains("food") || lower.contains("meal") || lower.contains("nutrition") || lower.contains("khana")) {
            if (isRomanUrdu) {
                if (goal.toLowerCase().contains("lose") || goal.toLowerCase().contains("fat")) {
                    return "Weight loss ke liye calorie deficit zaroori hai. Lean proteins (anda, chicken breast) aur vegetables zyada khaayein, aur processed sugar se door rahein.";
                } else {
                    return "Muscle gain aur weight build up ke liye healthy calorie surplus diet target karein. Oats, milk, eggs, nuts aur rice apne meals mein include karein.";
                }
            } else {
                if (goal.toLowerCase().contains("lose") || goal.toLowerCase().contains("fat")) {
                    return "For fat loss, focus on a calorie deficit. Fill your meals with low-calorie, high-protein options like eggs, chicken breast, steamed fish, leafy salads, and oats to stay full while cutting.";
                } else if (goal.toLowerCase().contains("gain") || goal.toLowerCase().contains("build") || goal.toLowerCase().contains("muscle")) {
                    return "To build lean muscle or gain weight, aim for a clean calorie surplus (+300 to +500 kcal). Consume calorie-dense whole foods like nuts, whole eggs, oats, milk, sweet potatoes, and avocados.";
                } else {
                    return "Aim for a balanced macro distribution: 40% complex carbs, 30% lean proteins, and 30% healthy fats. Prioritize whole foods over processed items for persistent energy.";
                }
            }
        }

        // 19. Workout & Exercise
        if (lower.contains("workout") || lower.contains("exercise") || lower.contains("routine") || lower.contains("plan") || lower.contains("gym")) {
            if (isRomanUrdu) {
                return "Aapko regular workouts schedule karne chahiye. Beginner hain to haftay mein 3 din 'Full Body Shred' ya Cardio start karein. Exercise se body energy boost hoti hai.";
            } else {
                if (goal.toLowerCase().contains("lose") || goal.toLowerCase().contains("fat")) {
                    return "For fat loss, target 3 HIIT cardio sessions per week paired with full-body strength training. This helps preserve lean muscle mass while elevating your calorie burn.";
                } else if (goal.toLowerCase().contains("gain") || goal.toLowerCase().contains("build") || goal.toLowerCase().contains("muscle")) {
                    return "To build muscle, practice progressive overload split across 3-4 days. Focus on compound lifts like squats, bench presses, and rows, aiming for 8-12 reps per set.";
                } else {
                    return "Maintain an active lifestyle by completing 20-30 minutes of mixed cardio/strength sessions 3 times a week, complemented by daily steps and stretches.";
                }
            }
        }

        // 20. Specific nutrients & parameters
        if (lower.contains("protein")) {
            double proteinMin = weight * 1.2;
            double proteinMax = weight * 2.0;
            return String.format("For active individuals weighing %.1f kg, you should target between %.1f g to %.1f g of protein daily. Focus on clean sources like chicken, fish, eggs, Greek yogurt, or plant-based proteins.", weight, proteinMin, proteinMax);
        }

        if (lower.contains("bmi") || lower.contains("body mass index")) {
            String category = getBmiCategory(bmi);
            return String.format("Your current calculated BMI is %.1f, which falls into the '%s' category. Regular tracking of your weight in the 'Track Progress' tab keeps these insights updated.", bmi, category);
        }

        if (lower.contains("water") || lower.contains("drink") || lower.contains("hydrate") || lower.contains("hydration")) {
            return "Hydration is essential for recovery. Target at least 2.5 to 3.0 liters of water daily. Increase this by 500 ml on workout days to compensate for sweat loss.";
        }

        if (lower.contains("sleep") || lower.contains("rest") || lower.contains("recovery")) {
            return "Muscle growth and fat loss happen during rest! Target 7-8 hours of quality sleep per night. Try to wind down 30 minutes before bed and avoid screens.";
        }

        if (lower.contains("motivation") || lower.contains("lazy") || lower.contains("tired") || lower.contains("hard")) {
            return "Motivation gets you started, but discipline keeps you going! Remember why you started this journey: to " + goal.toLowerCase() + ". Even a 10-minute light workout is better than doing nothing. You've got this!";
        }

        // Default smart fallback response matching language choice
        if (isRomanUrdu) {
            return "Aapka sawaal bohat dilchasp hai! Main aapki help ke liye yahan hoon. Kya aap is baare mein mazeed details jaan-na chahte hain?";
        } else {
            return "That is a very interesting question! I am here to help you. Would you like more details or have any follow-up questions about this topic?";
        }
    }

    private static String getBmiCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }
}
