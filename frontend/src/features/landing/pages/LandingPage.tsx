import { LandingHeader } from "../components/LandingHeader";
import { HeroSection } from "../components/HeroSection";
import { ProblemSection } from "../components/ProblemSection";
import { TransformationSection } from "../components/TransformationSection";
import { RelationshipsSection } from "../components/RelationshipsSection";
import { ExploreSection } from "../components/ExploreSection";
import { EvolutionSection } from "../components/EvolutionSection";
import { CollaborationSection } from "../components/CollaborationSection";
import { FinalCTA } from "../components/FinalCTA";
import { LandingFooter } from "../components/LandingFooter";

export function LandingPage() {
  return (
    <div className="min-h-screen bg-[hsl(220,42%,3%)] text-white">
      <LandingHeader />
      <main>
        <HeroSection />
        <ProblemSection />
        <TransformationSection />
        <RelationshipsSection />
        <ExploreSection />
        <EvolutionSection />
        <CollaborationSection />
        <FinalCTA />
      </main>
      <LandingFooter />
    </div>
  );
}
